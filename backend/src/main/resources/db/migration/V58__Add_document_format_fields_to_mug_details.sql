-- Add document format fields to article_mug_details table
-- These fields define the document format dimensions and margins for print layouts

alter table article_mug_details
    add column if not exists document_format_width_mm         integer,
    add column if not exists document_format_height_mm        integer,
    add column if not exists document_format_margin_bottom_mm integer;
