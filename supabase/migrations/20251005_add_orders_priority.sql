-- Add a priority column to orders to support admin prioritization
ALTER TABLE public.orders
ADD COLUMN IF NOT EXISTS priority integer NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.orders.priority IS 'Prioridade do pedido (maior = mais prioritário).';
