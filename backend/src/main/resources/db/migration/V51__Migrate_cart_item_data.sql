-- Data migration to populate foreign key columns from existing custom_data JSON
-- This migration extracts generatedImageId and promptId from custom_data and populates the new FK columns

-- Update cart_items with generated_image_id from custom_data
UPDATE cart_items 
SET generated_image_id = (custom_data->>'generatedImageId')::bigint
WHERE custom_data ? 'generatedImageId'
  AND custom_data->>'generatedImageId' IS NOT NULL
  AND custom_data->>'generatedImageId' != 'null'
  AND generated_image_id IS NULL
  AND EXISTS (
      SELECT 1 FROM generated_images gi 
      WHERE gi.id = (custom_data->>'generatedImageId')::bigint
  );

-- Update cart_items with prompt_id from custom_data
UPDATE cart_items 
SET prompt_id = (custom_data->>'promptId')::bigint
WHERE custom_data ? 'promptId'
  AND custom_data->>'promptId' IS NOT NULL
  AND custom_data->>'promptId' != 'null'
  AND prompt_id IS NULL
  AND EXISTS (
      SELECT 1 FROM prompts p 
      WHERE p.id = (custom_data->>'promptId')::bigint
  );

-- Log statistics of the migration (commented out as PostgreSQL doesn't support SELECT in migrations without stored procedures)
-- This would typically be logged in application logs during migration execution
-- SELECT 
--     COUNT(*) as total_cart_items,
--     COUNT(generated_image_id) as items_with_generated_image,
--     COUNT(prompt_id) as items_with_prompt
-- FROM cart_items;