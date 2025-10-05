-- Promote a specific user to admin role (idempotent)
-- User ID: 7ddc1038-639a-49a0-a2d0-ccb3446bed5f
-- This will insert into public.user_roles only if the user exists and the role is not already assigned.

insert into public.user_roles (user_id, role)
select '7ddc1038-639a-49a0-a2d0-ccb3446bed5f'::uuid, 'admin'::public.app_role
where exists (
  select 1 from auth.users where id = '7ddc1038-639a-49a0-a2d0-ccb3446bed5f'::uuid
)
on conflict (user_id, role) do nothing;

-- To revoke later (manual):
-- delete from public.user_roles where user_id = '7ddc1038-639a-49a0-a2d0-ccb3446bed5f' and role = 'admin';
