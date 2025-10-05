-- Create a public storage bucket for logos
insert into storage.buckets (id, name, public)
values ('logos', 'logos', true)
on conflict (id) do nothing;

-- Public read policy for logos
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Public can read logos'
  ) then
    create policy "Public can read logos" on storage.objects
      for select using (bucket_id = 'logos');
  end if;
end $$;

-- Admin write policies for logos
do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can insert logos'
  ) then
    create policy "Admins can insert logos" on storage.objects
      for insert with check (
        bucket_id = 'logos' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;

do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can update logos'
  ) then
    create policy "Admins can update logos" on storage.objects
      for update using (
        bucket_id = 'logos' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      ) with check (
        bucket_id = 'logos' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;

do $$ begin
  if not exists (
    select 1 from pg_policies where schemaname = 'storage' and tablename = 'objects' and policyname = 'Admins can delete logos'
  ) then
    create policy "Admins can delete logos" on storage.objects
      for delete using (
        bucket_id = 'logos' and exists(select 1 from public.user_roles ur where ur.user_id = auth.uid() and ur.role = 'admin')
      );
  end if;
end $$;
