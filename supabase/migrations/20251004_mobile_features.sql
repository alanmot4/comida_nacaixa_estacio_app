-- Storage bucket for marmitas images
insert into storage.buckets (id, name, public)
values ('marmitas', 'marmitas', true)
on conflict (id) do nothing;

-- Storage policies (public read/upload/update for now; tighten later to admins)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'storage' AND tablename = 'objects' AND policyname = 'marmitas_read'
  ) THEN
    CREATE POLICY marmitas_read ON storage.objects FOR SELECT USING (bucket_id = 'marmitas');
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'storage' AND tablename = 'objects' AND policyname = 'marmitas_upload'
  ) THEN
    CREATE POLICY marmitas_upload ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'marmitas');
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'storage' AND tablename = 'objects' AND policyname = 'marmitas_update'
  ) THEN
    CREATE POLICY marmitas_update ON storage.objects FOR UPDATE USING (bucket_id = 'marmitas') WITH CHECK (bucket_id = 'marmitas');
  END IF;
END $$;

-- Inventory table
create table if not exists public.inventory (
  id uuid primary key default gen_random_uuid(),
  marmita_id uuid not null references public.marmitas(id) on delete cascade,
  quantity integer not null default 0,
  updated_at timestamptz default now()
);

alter table public.inventory enable row level security;

-- Policies: public read, only admins manage
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'inventory' AND policyname = 'inventory_read_public'
  ) THEN
    CREATE POLICY inventory_read_public ON public.inventory FOR SELECT USING (true);
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'inventory' AND policyname = 'inventory_admin_manage'
  ) THEN
    CREATE POLICY inventory_admin_manage ON public.inventory FOR ALL USING (public.has_role(auth.uid(), 'admin')) WITH CHECK (public.has_role(auth.uid(), 'admin'));
  END IF;
END $$;

-- Payments table (mock)
create table if not exists public.payments (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  method text not null check (method in ('pix','dinheiro','cartao','mock')),
  amount numeric(10,2) not null,
  status text not null default 'pending' check (status in ('pending','approved','failed')),
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

alter table public.payments enable row level security;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'payments' AND policyname = 'payments_admin_read'
  ) THEN
    CREATE POLICY payments_admin_read ON public.payments FOR SELECT USING (public.has_role(auth.uid(), 'admin'));
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'payments' AND policyname = 'payments_admin_all'
  ) THEN
    CREATE POLICY payments_admin_all ON public.payments FOR ALL USING (public.has_role(auth.uid(), 'admin')) WITH CHECK (public.has_role(auth.uid(), 'admin'));
  END IF;
END $$;

-- Reports (views)
create or replace view public.report_daily_sales as
select date_trunc('day', created_at) as day,
       count(*) as orders_count,
       sum(total) as total_revenue
from public.orders
where status in ('confirmed','preparing','delivered')
group by 1
order by 1 desc;

create or replace view public.report_top_items as
select (item->>'id')::uuid as marmita_id,
       item->>'name' as name,
       sum((item->>'quantity')::int) as total_qty,
       sum(((item->>'price')::numeric) * ((item->>'quantity')::int)) as revenue
from public.orders o,
     lateral jsonb_array_elements(o.items) as item
group by 1,2
order by revenue desc;
