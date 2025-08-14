-- Add active column to article_mug_variants table
ALTER TABLE article_mug_variants ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Create index for better query performance when filtering by active status
CREATE INDEX idx_article_mug_variants_active ON article_mug_variants(active);