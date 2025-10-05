-- Add JSONB ingredients field to marmitas to store list of { name: text, grams: integer }
ALTER TABLE public.marmitas
ADD COLUMN IF NOT EXISTS ingredients jsonb DEFAULT '[]'::jsonb;

COMMENT ON COLUMN public.marmitas.ingredients IS 'Array de objetos { name: text, grams: integer } com os ingredientes e suas quantidades em gramas.';
