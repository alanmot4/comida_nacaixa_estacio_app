-- User profiles table tied to auth.users
create table if not exists public.profiles (
  id uuid primary key default auth.uid() references auth.users(id) on delete cascade,
  full_name text,
  phone text,
  updated_at timestamptz default now()
);

alter table public.profiles enable row level security;

-- Policies: user can read and modify only their own profile
do $$
begin
  if not exists (
    select 1 from pg_policies where schemaname = 'public' and tablename = 'profiles' and policyname = 'profiles_read_own'
  ) then
    create policy profiles_read_own on public.profiles for select using (auth.uid() = id);
  end if;
  if not exists (
    select 1 from pg_policies where schemaname = 'public' and tablename = 'profiles' and policyname = 'profiles_upsert_own'
  ) then
    create policy profiles_upsert_own on public.profiles for insert with check (auth.uid() = id);
  end if;
  if not exists (
    select 1 from pg_policies where schemaname = 'public' and tablename = 'profiles' and policyname = 'profiles_update_own'
  ) then
    create policy profiles_update_own on public.profiles for update using (auth.uid() = id) with check (auth.uid() = id);
  end if;
end $$;
