-- RLS performance fixes: avoid per-row initplans and duplicate permissive policies
-- This migration replaces auth.uid() with (select auth.uid()) in policies
-- and consolidates/selectively splits policies to reduce planner work.

-- NOTE: We intentionally do NOT create any index CONCURRENTLY here to avoid transaction issues.
-- user_roles already has UNIQUE(user_id, role) which provides a suitable index for lookups.

-- 1) PROFILES
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='profiles' and policyname='profiles_read_own') then
    drop policy "profiles_read_own" on public.profiles;
  end if;
  create policy "profiles_read_own" on public.profiles
    for select using ((select auth.uid()) = id);

  if exists (select 1 from pg_policies where schemaname='public' and tablename='profiles' and policyname='profiles_upsert_own') then
    drop policy "profiles_upsert_own" on public.profiles;
  end if;
  create policy "profiles_upsert_own" on public.profiles
    for insert with check ((select auth.uid()) = id);

  if exists (select 1 from pg_policies where schemaname='public' and tablename='profiles' and policyname='profiles_update_own') then
    drop policy "profiles_update_own" on public.profiles;
  end if;
  create policy "profiles_update_own" on public.profiles
    for update using ((select auth.uid()) = id) with check ((select auth.uid()) = id);
end $$;

-- 2) USER_ROLES: consolidate SELECT and use (select auth.uid())
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='user_roles' and policyname='Usuários podem ver suas próprias roles') then
    drop policy "Usuários podem ver suas próprias roles" on public.user_roles;
  end if;
  if exists (select 1 from pg_policies where schemaname='public' and tablename='user_roles' and policyname='Admins podem ver todas as roles') then
    drop policy "Admins podem ver todas as roles" on public.user_roles;
  end if;

  create policy "user_roles_select_combined" on public.user_roles
    for select using (
      public.has_role((select auth.uid()), 'admin')
      or ((select auth.uid()) = user_id)
    );

  if exists (select 1 from pg_policies where schemaname='public' and tablename='user_roles' and policyname='Admins podem inserir roles') then
    drop policy "Admins podem inserir roles" on public.user_roles;
  end if;
  create policy "Admins podem inserir roles" on public.user_roles
    for insert with check (public.has_role((select auth.uid()), 'admin'));

  if exists (select 1 from pg_policies where schemaname='public' and tablename='user_roles' and policyname='Admins podem atualizar roles') then
    drop policy "Admins podem atualizar roles" on public.user_roles;
  end if;
  create policy "Admins podem atualizar roles" on public.user_roles
    for update using (public.has_role((select auth.uid()), 'admin'));

  if exists (select 1 from pg_policies where schemaname='public' and tablename='user_roles' and policyname='Admins podem deletar roles') then
    drop policy "Admins podem deletar roles" on public.user_roles;
  end if;
  create policy "Admins podem deletar roles" on public.user_roles
    for delete using (public.has_role((select auth.uid()), 'admin'));
end $$;

-- 3) SETTINGS: split write policies; keep public read
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='settings' and policyname='Admins can write settings') then
    drop policy "Admins can write settings" on public.settings;
  end if;

  if not exists (select 1 from pg_policies where schemaname='public' and tablename='settings' and policyname='Admins can insert settings') then
    create policy "Admins can insert settings" on public.settings
      for insert with check (exists (
        select 1 from public.user_roles ur
        where ur.user_id = (select auth.uid()) and ur.role = 'admin'
      ));
  end if;

  if not exists (select 1 from pg_policies where schemaname='public' and tablename='settings' and policyname='Admins can update settings') then
    create policy "Admins can update settings" on public.settings
      for update using (exists (
        select 1 from public.user_roles ur
        where ur.user_id = (select auth.uid()) and ur.role = 'admin'
      )) with check (exists (
        select 1 from public.user_roles ur
        where ur.user_id = (select auth.uid()) and ur.role = 'admin'
      ));
  end if;

  if not exists (select 1 from pg_policies where schemaname='public' and tablename='settings' and policyname='Admins can delete settings') then
    create policy "Admins can delete settings" on public.settings
      for delete using (exists (
        select 1 from public.user_roles ur
        where ur.user_id = (select auth.uid()) and ur.role = 'admin'
      ));
  end if;
end $$;

-- 4) INVENTORY: explicit select policy + split write
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='inventory' and policyname='inventory_read_public') then
    drop policy "inventory_read_public" on public.inventory;
  end if;
  create policy "inventory_read_public" on public.inventory
    for select using (true);

  if exists (select 1 from pg_policies where schemaname='public' and tablename='inventory' and policyname='inventory_admin_manage') then
    drop policy "inventory_admin_manage" on public.inventory;
  end if;

  create policy "inventory_admin_insert" on public.inventory
    for insert with check (public.has_role((select auth.uid()), 'admin'));
  create policy "inventory_admin_update" on public.inventory
    for update using (public.has_role((select auth.uid()), 'admin'));
  create policy "inventory_admin_delete" on public.inventory
    for delete using (public.has_role((select auth.uid()), 'admin'));
end $$;

-- 5) PAYMENTS: split SELECT and write
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='payments' and policyname='payments_admin_read') then
    drop policy "payments_admin_read" on public.payments;
  end if;
  create policy "payments_admin_read" on public.payments
    for select using (public.has_role((select auth.uid()), 'admin'));

  if exists (select 1 from pg_policies where schemaname='public' and tablename='payments' and policyname='payments_admin_all') then
    drop policy "payments_admin_all" on public.payments;
  end if;
  create policy "payments_admin_insert" on public.payments
    for insert with check (public.has_role((select auth.uid()), 'admin'));
  create policy "payments_admin_update" on public.payments
    for update using (public.has_role((select auth.uid()), 'admin')) with check (public.has_role((select auth.uid()), 'admin'));
  create policy "payments_admin_delete" on public.payments
    for delete using (public.has_role((select auth.uid()), 'admin'));
end $$;

-- 6) ORDERS: consolidate SELECT; leave only one INSERT
do $$
begin
  if exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Admins podem ver todos os pedidos') then
    drop policy "Admins podem ver todos os pedidos" on public.orders;
  end if;
  if exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Clientes podem ver seus próprios pedidos') then
    drop policy "Clientes podem ver seus próprios pedidos" on public.orders;
  end if;

  create policy "orders_select_combined" on public.orders
    for select using (
      public.has_role((select auth.uid()), 'admin')
      or (
        (select auth.uid()) is not null
        and public.has_role((select auth.uid()), 'cliente')
      )
    );

  if exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Clientes podem criar pedidos') then
    drop policy "Clientes podem criar pedidos" on public.orders;
  end if;
  if exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Toten pode criar pedidos') then
    drop policy "Toten pode criar pedidos" on public.orders;
  end if;
  if not exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Qualquer um pode criar pedidos') then
    create policy "Qualquer um pode criar pedidos" on public.orders
      for insert with check (true);
  end if;

  if exists (select 1 from pg_policies where schemaname='public' and tablename='orders' and policyname='Admins podem atualizar pedidos') then
    drop policy "Admins podem atualizar pedidos" on public.orders;
  end if;
  create policy "Admins podem atualizar pedidos" on public.orders
    for update using (public.has_role((select auth.uid()), 'admin'));
end $$;

-- 7) CATEGORIES & MARMITAS: wrap auth.uid() in select
do $$
begin
  -- CATEGORIES
  if exists (select 1 from pg_policies where schemaname='public' and tablename='categories' and policyname='Admins podem inserir categorias') then
    drop policy "Admins podem inserir categorias" on public.categories;
    create policy "Admins podem inserir categorias" on public.categories
      for insert with check (public.has_role((select auth.uid()), 'admin'));
  end if;

  if exists (select 1 from pg_policies where schemaname='public' and tablename='categories' and policyname='Admins podem atualizar categorias') then
    drop policy "Admins podem atualizar categorias" on public.categories;
    create policy "Admins podem atualizar categorias" on public.categories
      for update using (public.has_role((select auth.uid()), 'admin'));
  end if;

  if exists (select 1 from pg_policies where schemaname='public' and tablename='categories' and policyname='Admins podem deletar categorias') then
    drop policy "Admins podem deletar categorias" on public.categories;
    create policy "Admins podem deletar categorias" on public.categories
      for delete using (public.has_role((select auth.uid()), 'admin'));
  end if;

  -- MARMITAS
  if exists (select 1 from pg_policies where schemaname='public' and tablename='marmitas' and policyname='Admins podem inserir marmitas') then
    drop policy "Admins podem inserir marmitas" on public.marmitas;
    create policy "Admins podem inserir marmitas" on public.marmitas
      for insert with check (public.has_role((select auth.uid()), 'admin'));
  end if;

  if exists (select 1 from pg_policies where schemaname='public' and tablename='marmitas' and policyname='Admins podem atualizar marmitas') then
    drop policy "Admins podem atualizar marmitas" on public.marmitas;
    create policy "Admins podem atualizar marmitas" on public.marmitas
      for update using (public.has_role((select auth.uid()), 'admin'));
  end if;

  if exists (select 1 from pg_policies where schemaname='public' and tablename='marmitas' and policyname='Admins podem deletar marmitas') then
    drop policy "Admins podem deletar marmitas" on public.marmitas;
    create policy "Admins podem deletar marmitas" on public.marmitas
      for delete using (public.has_role((select auth.uid()), 'admin'));
  end if;
end $$;

-- 8) STORAGE: wrap auth.uid() with select (banners/logos)
do $$
begin
  -- BANNERS
  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can insert banners') then
    drop policy "Admins can insert banners" on storage.objects;
    create policy "Admins can insert banners" on storage.objects
      for insert with check (
        bucket_id = 'banners' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;

  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can update banners') then
    drop policy "Admins can update banners" on storage.objects;
    create policy "Admins can update banners" on storage.objects
      for update using (
        bucket_id = 'banners' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      ) with check (
        bucket_id = 'banners' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;

  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can delete banners') then
    drop policy "Admins can delete banners" on storage.objects;
    create policy "Admins can delete banners" on storage.objects
      for delete using (
        bucket_id = 'banners' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;

  -- LOGOS
  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can insert logos') then
    drop policy "Admins can insert logos" on storage.objects;
    create policy "Admins can insert logos" on storage.objects
      for insert with check (
        bucket_id = 'logos' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;

  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can update logos') then
    drop policy "Admins can update logos" on storage.objects;
    create policy "Admins can update logos" on storage.objects
      for update using (
        bucket_id = 'logos' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      ) with check (
        bucket_id = 'logos' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;

  if exists (select 1 from pg_policies where schemaname='storage' and tablename='objects' and policyname='Admins can delete logos') then
    drop policy "Admins can delete logos" on storage.objects;
    create policy "Admins can delete logos" on storage.objects
      for delete using (
        bucket_id = 'logos' and exists(
          select 1 from public.user_roles ur where ur.user_id = (select auth.uid()) and ur.role = 'admin'
        )
      );
  end if;
end $$;
