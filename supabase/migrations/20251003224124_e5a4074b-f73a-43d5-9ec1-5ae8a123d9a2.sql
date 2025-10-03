-- Tabela de categorias de marmitas
CREATE TABLE public.categories (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  created_at timestamp with time zone DEFAULT now()
);

-- Tabela de marmitas/produtos
CREATE TABLE public.marmitas (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  description text,
  price decimal(10,2) NOT NULL,
  image_url text,
  category_id uuid REFERENCES public.categories(id),
  available boolean DEFAULT true,
  created_at timestamp with time zone DEFAULT now()
);

-- Tabela de pedidos
CREATE TABLE public.orders (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_name text NOT NULL,
  customer_phone text NOT NULL,
  customer_address text NOT NULL,
  items jsonb NOT NULL,
  total decimal(10,2) NOT NULL,
  status text DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'preparing', 'delivered', 'cancelled')),
  notes text,
  created_at timestamp with time zone DEFAULT now()
);

-- Habilitar RLS
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.marmitas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

-- Políticas RLS - Leitura pública para categorias e marmitas
CREATE POLICY "Todos podem visualizar categorias" 
ON public.categories FOR SELECT 
USING (true);

CREATE POLICY "Todos podem visualizar marmitas" 
ON public.marmitas FOR SELECT 
USING (available = true);

-- Políticas para pedidos - qualquer um pode criar
CREATE POLICY "Qualquer um pode criar pedidos" 
ON public.orders FOR INSERT 
WITH CHECK (true);

-- Inserir categorias iniciais
INSERT INTO public.categories (name) VALUES 
  ('Tradicional'),
  ('Fitness'),
  ('Vegetariana'),
  ('Executiva');

-- Inserir marmitas de exemplo
INSERT INTO public.marmitas (name, description, price, category_id, available) VALUES
  ('Marmita Tradicional', 'Arroz, feijão, bife acebolado, batata frita e salada', 18.90, (SELECT id FROM public.categories WHERE name = 'Tradicional'), true),
  ('Marmita Fitness', 'Arroz integral, frango grelhado, batata doce e legumes', 22.90, (SELECT id FROM public.categories WHERE name = 'Fitness'), true),
  ('Marmita Vegetariana', 'Arroz, feijão, PVT, legumes refogados e salada', 19.90, (SELECT id FROM public.categories WHERE name = 'Vegetariana'), true),
  ('Marmita Executiva', 'Arroz, feijão, picanha, farofa, vinagrete e salada', 28.90, (SELECT id FROM public.categories WHERE name = 'Executiva'), true);