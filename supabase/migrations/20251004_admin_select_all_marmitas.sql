-- Allow admins to view all marmitas (not only available=true)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_policies WHERE schemaname = 'public' AND tablename = 'marmitas' AND policyname = 'Admins podem visualizar todas as marmitas'
  ) THEN
    CREATE POLICY "Admins podem visualizar todas as marmitas"
    ON public.marmitas FOR SELECT
    USING (public.has_role(auth.uid(), 'admin'));
  END IF;
END $$;
