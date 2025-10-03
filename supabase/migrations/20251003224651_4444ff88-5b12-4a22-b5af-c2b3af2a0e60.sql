-- Criar enum para as roles
CREATE TYPE public.app_role AS ENUM ('admin', 'cliente', 'toten');

-- Criar tabela de roles de usuários
CREATE TABLE public.user_roles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
  role public.app_role NOT NULL,
  created_at timestamp with time zone DEFAULT now(),
  UNIQUE (user_id, role)
);

-- Habilitar RLS
ALTER TABLE public.user_roles ENABLE ROW LEVEL SECURITY;

-- Criar função security definer para verificar roles (evita recursão no RLS)
CREATE OR REPLACE FUNCTION public.has_role(_user_id uuid, _role public.app_role)
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM public.user_roles
    WHERE user_id = _user_id
      AND role = _role
  )
$$;

-- Políticas RLS para user_roles
CREATE POLICY "Usuários podem ver suas próprias roles"
ON public.user_roles FOR SELECT
USING (auth.uid() = user_id);

CREATE POLICY "Admins podem ver todas as roles"
ON public.user_roles FOR SELECT
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem inserir roles"
ON public.user_roles FOR INSERT
WITH CHECK (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem atualizar roles"
ON public.user_roles FOR UPDATE
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem deletar roles"
ON public.user_roles FOR DELETE
USING (public.has_role(auth.uid(), 'admin'));

-- Atualizar políticas de marmitas para admins poderem gerenciar
CREATE POLICY "Admins podem inserir marmitas"
ON public.marmitas FOR INSERT
WITH CHECK (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem atualizar marmitas"
ON public.marmitas FOR UPDATE
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem deletar marmitas"
ON public.marmitas FOR DELETE
USING (public.has_role(auth.uid(), 'admin'));

-- Atualizar políticas de categorias para admins
CREATE POLICY "Admins podem inserir categorias"
ON public.categories FOR INSERT
WITH CHECK (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem atualizar categorias"
ON public.categories FOR UPDATE
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Admins podem deletar categorias"
ON public.categories FOR DELETE
USING (public.has_role(auth.uid(), 'admin'));

-- Políticas para pedidos
CREATE POLICY "Admins podem ver todos os pedidos"
ON public.orders FOR SELECT
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Clientes podem ver seus próprios pedidos"
ON public.orders FOR SELECT
USING (
  auth.uid() IS NOT NULL AND
  public.has_role(auth.uid(), 'cliente')
);

CREATE POLICY "Admins podem atualizar pedidos"
ON public.orders FOR UPDATE
USING (public.has_role(auth.uid(), 'admin'));

CREATE POLICY "Toten pode criar pedidos"
ON public.orders FOR INSERT
WITH CHECK (public.has_role(auth.uid(), 'toten'));

CREATE POLICY "Clientes podem criar pedidos"
ON public.orders FOR INSERT
WITH CHECK (public.has_role(auth.uid(), 'cliente'));