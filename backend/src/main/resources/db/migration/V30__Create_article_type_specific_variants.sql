-- create article_mug_variants table
create table if not exists article_mug_variants (
    id bigserial primary key,
    article_id bigint not null references articles(id) on delete cascade,
    inside_color_code varchar(255) not null default '#ffffff',
    outside_color_code varchar(255) not null default '#ffffff',
    name varchar(255) not null,
    sku varchar(255) unique,
    example_image_filename varchar(500),
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_shirt_variants table
create table if not exists article_shirt_variants (
    id bigserial primary key,
    article_id bigint not null references articles(id) on delete cascade,
    color varchar(255) not null,
    size varchar(50) not null,
    sku varchar(255) unique,
    example_image_filename varchar(500),
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_pillow_variants table
create table if not exists article_pillow_variants (
    id bigserial primary key,
    article_id bigint not null references articles(id) on delete cascade,
    color varchar(255) not null,
    material varchar(255) not null,
    sku varchar(255) unique,
    example_image_filename varchar(500),
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create indexes
create index idx_article_mug_variants_article_id on article_mug_variants(article_id);
create index idx_article_shirt_variants_article_id on article_shirt_variants(article_id);
create index idx_article_pillow_variants_article_id on article_pillow_variants(article_id);

-- migrate existing data from article_variants
-- migrate mug variants
insert into article_mug_variants (article_id, name, outside_color_code, inside_color_code, sku, example_image_filename, created_at, updated_at)
select 
    av.article_id,
    av.variant_value as name,
    '#ffffff' as outside_color_code,
    '#ffffff' as inside_color_code,
    av.sku,
    av.example_image_filename,
    av.created_at,
    av.updated_at
from article_variants av
join articles a on av.article_id = a.id
where a.article_type = 'MUG' and av.variant_type = 'COLOR';

-- migrate shirt variants
insert into article_shirt_variants (article_id, color, size, sku, example_image_filename, created_at, updated_at)
select 
    av.article_id,
    case when av.variant_type = 'COLOR' then av.variant_value else 'Default' end as color,
    case when av.variant_type = 'SIZE' then av.variant_value else 'M' end as size,
    av.sku,
    av.example_image_filename,
    av.created_at,
    av.updated_at
from article_variants av
join articles a on av.article_id = a.id
where a.article_type = 'SHIRT';

-- migrate pillow variants
insert into article_pillow_variants (article_id, color, material, sku, example_image_filename, created_at, updated_at)
select 
    av.article_id,
    case when av.variant_type = 'COLOR' then av.variant_value else 'Default' end as color,
    case when av.variant_type = 'MATERIAL' then av.variant_value else 'Cotton' end as material,
    av.sku,
    av.example_image_filename,
    av.created_at,
    av.updated_at
from article_variants av
join articles a on av.article_id = a.id
where a.article_type = 'PILLOW';

-- drop the old article_variants table
drop table if exists article_variants;