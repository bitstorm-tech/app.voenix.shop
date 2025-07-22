-- create articles table
create table if not exists articles (
    id bigserial primary key,
    name varchar(255) not null,
    description_short text not null,
    description_long text not null,
    main_image varchar(500) not null,
    price integer not null,
    active boolean default true,
    article_type varchar(50) not null,
    category_id bigint not null references article_categories(id),
    subcategory_id bigint references article_sub_categories(id),
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_mug_details table
create table if not exists article_mug_details (
    article_id bigint primary key references articles(id) on delete cascade,
    height_mm integer not null,
    diameter_mm integer not null,
    print_template_width_mm integer not null,
    print_template_height_mm integer not null,
    filling_quantity varchar(50),
    dishwasher_safe boolean default true,
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_shirt_details table
create table if not exists article_shirt_details (
    article_id bigint primary key references articles(id) on delete cascade,
    material varchar(255) not null,
    care_instructions text,
    fit_type varchar(50) not null,
    available_sizes text[] not null,
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_pillow_details table
create table if not exists article_pillow_details (
    article_id bigint primary key references articles(id) on delete cascade,
    width_cm integer not null,
    height_cm integer not null,
    depth_cm integer not null,
    material varchar(255) not null,
    filling_type varchar(255) not null,
    cover_removable boolean default true,
    washable boolean default true,
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create article_variants table
create table if not exists article_variants (
    id bigserial primary key,
    article_id bigint not null references articles(id) on delete cascade,
    variant_type varchar(50) not null,
    variant_value varchar(255) not null,
    sku varchar(255) unique,
    example_image_filename varchar(500),
    created_at timestamptz default current_timestamp,
    updated_at timestamptz default current_timestamp
);

-- create indexes
create index idx_articles_name on articles(name);
create index idx_articles_active on articles(active);
create index idx_articles_type on articles(article_type);
create index idx_articles_category_id on articles(category_id);
create index idx_articles_subcategory_id on articles(subcategory_id);
create index idx_article_variants_article_id on article_variants(article_id);
create index idx_article_variants_type on article_variants(variant_type);
create index idx_article_variants_sku on article_variants(sku);

-- drop old mug tables
drop table if exists mug_variants;
drop table if exists mugs;