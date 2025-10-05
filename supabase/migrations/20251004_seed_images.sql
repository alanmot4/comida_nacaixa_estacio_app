-- Set image_url for seeded marmitas (upload the files to the 'marmitas' bucket first)
-- Expected files in bucket: marmita-tradicional.jpg, marmita-fitness.jpg, marmita-vegetariana.jpg, marmita-executiva.jpg
-- Replace the project ref if different
update public.marmitas
set image_url = 'https://khvsdhyzujfxofzgoijt.supabase.co/storage/v1/object/public/marmitas/marmita-tradicional.jpg'
where name = 'Marmita Tradicional';

update public.marmitas
set image_url = 'https://khvsdhyzujfxofzgoijt.supabase.co/storage/v1/object/public/marmitas/marmita-fitness.jpg'
where name = 'Marmita Fitness';

update public.marmitas
set image_url = 'https://khvsdhyzujfxofzgoijt.supabase.co/storage/v1/object/public/marmitas/marmita-vegetariana.jpg'
where name = 'Marmita Vegetariana';

update public.marmitas
set image_url = 'https://khvsdhyzujfxofzgoijt.supabase.co/storage/v1/object/public/marmitas/marmita-executiva.jpg'
where name = 'Marmita Executiva';
