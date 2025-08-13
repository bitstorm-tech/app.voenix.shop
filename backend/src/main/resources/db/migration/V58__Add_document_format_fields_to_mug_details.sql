-- Add document format fields to article_mug_details table
-- These fields define the document format dimensions and margins for print layouts

ALTER TABLE article_mug_details
ADD COLUMN document_format_width_mm INTEGER,
ADD COLUMN document_format_height_mm INTEGER,
ADD COLUMN document_format_margin_bottom_mm INTEGER;
