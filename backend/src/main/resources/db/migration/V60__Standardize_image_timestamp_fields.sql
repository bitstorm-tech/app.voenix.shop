-- Standardize timestamp fields in image tables
-- Replace uploadedAt and generatedAt with createdAt for consistency

-- Rename uploaded_at to created_at in uploaded_images table
ALTER TABLE uploaded_images RENAME COLUMN uploaded_at TO created_at;
ALTER TABLE uploaded_images ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- Rename generated_at to created_at in generated_images table
ALTER TABLE generated_images RENAME COLUMN generated_at TO created_at;
ALTER TABLE generated_images ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- Update indexes for uploaded_images
DROP INDEX IF EXISTS idx_uploaded_images_uploaded_at;
CREATE INDEX idx_uploaded_images_created_at ON uploaded_images(created_at);

-- Update indexes for generated_images
DROP INDEX IF EXISTS idx_generated_images_generated_at;
CREATE INDEX idx_generated_images_created_at ON generated_images(created_at);

DROP INDEX IF EXISTS idx_generated_images_user_generated;
CREATE INDEX idx_generated_images_user_created ON generated_images(user_id, created_at);

DROP INDEX IF EXISTS idx_generated_images_ip_generated;
CREATE INDEX idx_generated_images_ip_created ON generated_images(ip_address, created_at);

-- Update column comments
COMMENT ON COLUMN uploaded_images.created_at IS 'Timestamp when the image was uploaded';
COMMENT ON COLUMN generated_images.created_at IS 'Timestamp when the image was generated';