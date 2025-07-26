-- add supplier_article_number and is_default columns to article_mug_variants table
alter table article_mug_variants 
    add column supplier_article_number varchar(100),
    add column is_default boolean not null default false;

-- set the first variant as default for each existing mug
update article_mug_variants amv
set is_default = true
where amv.id = (
    select min(id) 
    from article_mug_variants 
    where article_id = amv.article_id
);

-- add unique constraint to ensure only one default variant per mug
create unique index idx_article_mug_variants_one_default_per_article 
on article_mug_variants (article_id) 
where is_default = true;

-- add index for supplier article number for faster lookups
create index idx_article_mug_variants_supplier_article_number 
on article_mug_variants (supplier_article_number) 
where supplier_article_number is not null;