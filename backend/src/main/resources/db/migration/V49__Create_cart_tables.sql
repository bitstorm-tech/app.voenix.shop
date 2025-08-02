-- carts table
create table carts (
    id bigserial primary key,
    user_id bigint not null references users(id),
    status varchar(20) not null default 'active',
    version bigint not null default 0, -- For optimistic locking
    expires_at timestamptz,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint chk_cart_status check (status in ('active', 'abandoned', 'converted'))
);

-- cart_items table  
create table cart_items (
    id bigserial primary key,
    cart_id bigint not null references carts(id) on delete cascade,
    article_id bigint not null references articles(id),
    variant_id bigint not null references article_mug_variants(id),
    quantity int not null check (quantity > 0),
    price_at_time int not null, -- Price in cents
    original_price int not null, -- Original price for comparison
    custom_data jsonb not null default '{}',
    position int not null default 0,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

-- Indexes for performance
create index idx_carts_user_id on carts(user_id);
create index idx_carts_status_updated_at on carts(status, updated_at);

-- Partial unique index to ensure a user can only have one active cart
create unique index uk_user_active_cart on carts(user_id) where status = 'active';
create index idx_cart_items_cart_id_position on cart_items(cart_id, position);
create index idx_cart_items_article_variant on cart_items(article_id, variant_id);
create index idx_cart_items_custom_data_gin on cart_items using gin(custom_data);

-- Unique constraint for cart items (considering custom data)
create unique index uk_cart_item_unique on cart_items (
    cart_id, 
    article_id, 
    variant_id,
    md5(custom_data::text) -- Hash of custom data for uniqueness
);