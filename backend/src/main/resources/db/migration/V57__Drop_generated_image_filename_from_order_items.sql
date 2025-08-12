-- Drop the redundant generated_image_filename column from order_items table
-- The filename can be fetched dynamically using generated_image_id
alter table order_items drop column if exists generated_image_filename;