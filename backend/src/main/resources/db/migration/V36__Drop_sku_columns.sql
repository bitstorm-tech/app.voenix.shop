-- drop sku columns from article variant tables
alter table article_mug_variants drop column if exists sku;
alter table article_shirt_variants drop column if exists sku;
alter table article_pillow_variants drop column if exists sku;