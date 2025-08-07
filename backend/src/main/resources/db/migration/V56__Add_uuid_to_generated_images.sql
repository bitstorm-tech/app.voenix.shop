-- Add UUID column to generated_images table
alter table generated_images add column uuid uuid;

-- Update existing records with generated UUIDs
update generated_images set uuid = gen_random_uuid() where uuid is null;

-- Make the UUID column not null and unique
alter table generated_images alter column uuid set not null;
alter table generated_images add constraint uk_generated_images_uuid unique (uuid);