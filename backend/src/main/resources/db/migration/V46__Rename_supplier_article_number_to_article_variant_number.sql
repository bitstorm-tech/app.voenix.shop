-- rename supplier_article_number column to article_variant_number in article_mug_variants table
alter table article_mug_variants 
    rename column supplier_article_number to article_variant_number;

-- rename index to match new column name
drop index if exists idx_article_mug_variants_supplier_article_number;
create index idx_article_mug_variants_article_variant_number 
on article_mug_variants (article_variant_number) 
where article_variant_number is not null;