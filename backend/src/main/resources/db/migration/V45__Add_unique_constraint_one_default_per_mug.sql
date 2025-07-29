-- Add partial unique index to ensure only one default variant per mug article
-- This allows multiple non-default variants but only one with is_default = true
create unique index idx_one_default_per_mug on article_mug_variants(article_id) where is_default = true;