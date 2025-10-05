-- Estoque: tabela e vínculo com marmitas
CREATE TABLE IF NOT EXISTS public.inventory (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  marmita_id uuid NOT NULL REFERENCES public.marmitas(id) ON DELETE CASCADE,
  quantity integer NOT NULL DEFAULT 0,
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.inventory ENABLE ROW LEVEL SECURITY;

-- Políticas de estoque: leitura pública, escrita somente admin
DO $$ BEGIN
  CREATE POLICY "Todos podem ver estoque"
  ON public.inventory FOR SELECT
  USING (true);
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE POLICY "Admins podem gerenciar estoque"
  ON public.inventory FOR ALL
  USING (public.has_role(auth.uid(), 'admin'))
  WITH CHECK (public.has_role(auth.uid(), 'admin'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Pagamentos mock (sem integrar gateway real)
CREATE TABLE IF NOT EXISTS public.payments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id uuid NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  amount decimal(10,2) NOT NULL,
  method text NOT NULL DEFAULT 'pix',
  status text NOT NULL DEFAULT 'approved' CHECK (status IN ('approved','declined','pending')),
  created_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;

DO $$ BEGIN
  CREATE POLICY "Admins podem ver todos os pagamentos"
  ON public.payments FOR SELECT
  USING (public.has_role(auth.uid(), 'admin'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE POLICY "Admins podem inserir pagamentos"
  ON public.payments FOR INSERT
  WITH CHECK (public.has_role(auth.uid(), 'admin'));
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Relatórios simples (views)
CREATE OR REPLACE VIEW public.report_daily_sales AS
SELECT date_trunc('day', created_at) AS day,
       COUNT(*)                        AS orders,
       SUM(total)                      AS revenue
FROM public.orders
GROUP BY 1
ORDER BY 1 DESC;

CREATE OR REPLACE VIEW public.report_top_items AS
SELECT (jsonb_array_elements(items)->>'name') AS item_name,
       SUM(((jsonb_array_elements(items)->>'quantity')::int)) AS qty,
       SUM(((jsonb_array_elements(items)->>'price')::numeric) * ((jsonb_array_elements(items)->>'quantity')::int)) AS total
FROM public.orders
GROUP BY 1
ORDER BY qty DESC;

-- Views como leitura pública de relatórios agregados (sem dados sensíveis)
ALTER VIEW public.report_daily_sales SET (security_invoker = on);
ALTER VIEW public.report_top_items SET (security_invoker = on);