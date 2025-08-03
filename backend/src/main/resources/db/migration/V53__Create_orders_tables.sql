-- Create sequence for order numbers
CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 10000;

-- orders table
create table orders (
    id uuid primary key default gen_random_uuid(),
    order_number varchar(50) not null unique default 'ORD-' || nextval('order_number_seq'),
    user_id bigint not null references users(id),
    customer_email varchar(255) not null,
    customer_first_name varchar(255) not null,
    customer_last_name varchar(255) not null,
    customer_phone varchar(50),
    -- Shipping address
    shipping_street_address_1 varchar(255) not null,
    shipping_street_address_2 varchar(255),
    shipping_city varchar(100) not null,
    shipping_state varchar(100) not null,
    shipping_postal_code varchar(20) not null,
    shipping_country varchar(100) not null,
    -- Billing address (nullable, same as shipping if null)
    billing_street_address_1 varchar(255),
    billing_street_address_2 varchar(255),
    billing_city varchar(100),
    billing_state varchar(100),
    billing_postal_code varchar(20),
    billing_country varchar(100),
    -- Pricing (all in cents)
    subtotal bigint not null,
    tax_amount bigint not null,
    shipping_amount bigint not null,
    total_amount bigint not null,
    status varchar(20) not null default 'PENDING',
    cart_id bigint not null references carts(id),
    notes text,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    constraint chk_order_status check (status in ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    constraint chk_order_amounts_positive check (subtotal >= 0 and tax_amount >= 0 and shipping_amount >= 0 and total_amount >= 0),
    constraint chk_total_calculation check (total_amount = subtotal + tax_amount + shipping_amount)
);

-- order_items table
create table order_items (
    id uuid primary key default gen_random_uuid(),
    order_id uuid not null references orders(id) on delete cascade,
    article_id bigint not null references articles(id),
    variant_id bigint not null references article_mug_variants(id),
    quantity int not null check (quantity > 0),
    price_per_item bigint not null check (price_per_item >= 0), -- Price in cents at time of order
    total_price bigint not null check (total_price >= 0), -- Total price for line item
    generated_image_id bigint,
    generated_image_filename varchar(255),
    prompt_id bigint references prompts(id),
    custom_data jsonb not null default '{}',
    created_at timestamptz not null default current_timestamp,
    constraint chk_order_item_total_price check (total_price = price_per_item * quantity)
);

-- Indexes for performance
create index idx_orders_user_id on orders(user_id);
create index idx_orders_status on orders(status);
create index idx_orders_created_at on orders(created_at);
create index idx_orders_order_number on orders(order_number);
create index idx_orders_cart_id on orders(cart_id);

create index idx_order_items_order_id on order_items(order_id);
create index idx_order_items_article_variant on order_items(article_id, variant_id);
create index idx_order_items_created_at on order_items(created_at);
create index idx_order_items_custom_data_gin on order_items using gin(custom_data);