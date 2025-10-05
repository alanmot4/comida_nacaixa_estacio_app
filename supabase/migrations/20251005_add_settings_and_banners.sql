-- Settings table for app-wide configurations (e.g., banner image)
create table if not exists public.settings (
  key text primary key,
  value text,
  updated_at timestamptz default now()
);

alter table public.settings enable row level security;

-- Allow anyone to read settings (public configuration)
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'public' and tablename = 'settings' and policyname = 'Public can read settings'
  ) then
    create policy "Public can read settings" on public.settings
      for select using (true);
  end if;
end $$;

-- Only admins can insert/update/delete settings
-- Assumes user_roles table with role 'admin' and function auth.uid() available
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'public' and tablename = 'settings' and policyname = 'Admins can write settings'
  ) then
    create policy "Admins can write settings" on public.settings
      for all using (exists (
        select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin'
      )) with check (exists (
        select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin'
      ));
  end if;
end $$;

-- Create a public storage bucket for banners
insert into storage.buckets (id, name, public)
values ('banners', 'banners', true)
on conflict (id) do nothing;

-- Storage policies for banners bucket
-- Public read
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Public can read banners'
  ) then
    create policy "Public can read banners" on storage.objects
      for select using (bucket_id = 'banners');
  end if;
end $$;

-- Admin write (insert/update/delete)
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can insert banners'
  ) then
    create policy "Admins can insert banners" on storage.objects
      for insert with check (
        bucket_id = 'banners' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;

do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can update banners'
  ) then
    create policy "Admins can update banners" on storage.objects
      for update using (
        bucket_id = 'banners' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      ) with check (
        bucket_id = 'banners' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;

do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can delete banners'
  ) then
    create policy "Admins can delete banners" on storage.objects
      for delete using (
        bucket_id = 'banners' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;
