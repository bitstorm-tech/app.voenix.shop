create table if not exists flyway_schema_history
(
    installed_rank integer                 not null,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null,
    constraint flyway_schema_history_pk
        primary key (installed_rank)
);

create index if not exists flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table if not exists users
(
    id                           bigserial,
    email                        varchar(255)                                       not null,
    first_name                   varchar(255),
    last_name                    varchar(255),
    phone_number                 varchar(255),
    password                     varchar(255),
    one_time_password            varchar(255),
    one_time_password_created_at timestamp with time zone,
    created_at                   timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at                   timestamp with time zone default CURRENT_TIMESTAMP,
    deleted_at                   timestamp with time zone,
    primary key (id),
    unique (email)
);

create index if not exists idx_users_email
    on users (email);

create index if not exists idx_users_deleted_at
    on users (deleted_at);

create index if not exists idx_users_active
    on users (id)
    where (deleted_at IS NULL);

create unique index if not exists idx_users_active_email
    on users (email)
    where (deleted_at IS NULL);

create table if not exists prompt_categories
(
    id         bigserial,
    name       varchar(255)                                       not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (id),
    unique (name)
);

create index if not exists idx_prompt_categories_name
    on prompt_categories (name);

create table if not exists article_categories
(
    id          bigint                   default nextval('mug_categories_id_seq'::regclass) not null,
    name        varchar(255)                                                                not null,
    description text,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP                          not null,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP                          not null,
    constraint mug_categories_pkey
        primary key (id)
);

create index if not exists idx_article_categories_name
    on article_categories (name);

create table if not exists article_sub_categories
(
    id                  bigint                   default nextval('mug_sub_categories_id_seq'::regclass) not null,
    article_category_id bigint                                                                          not null,
    name                varchar(255)                                                                    not null,
    description         text,
    created_at          timestamp with time zone default CURRENT_TIMESTAMP                              not null,
    updated_at          timestamp with time zone default CURRENT_TIMESTAMP                              not null,
    constraint mug_sub_categories_pkey
        primary key (id),
    constraint fk_article_sub_categories_category
        foreign key (article_category_id) references article_categories
);

create index if not exists idx_article_sub_categories_category_id
    on article_sub_categories (article_category_id);

create index if not exists idx_article_sub_categories_name
    on article_sub_categories (name);

create unique index if not exists idx_article_sub_categories_category_name
    on article_sub_categories (article_category_id, lower(name::text));

create table if not exists prompt_slot_types
(
    id         bigint                   default nextval('slot_types_id_seq'::regclass) not null,
    name       varchar(255)                                                            not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP                      not null,
    updated_at timestamp with time zone default CURRENT_TIMESTAMP                      not null,
    position   integer                  default 0                                      not null,
    constraint slot_types_pkey
        primary key (id),
    constraint slot_types_name_key
        unique (name),
    constraint uk_prompt_slot_types_position
        unique (position)
);

create index if not exists idx_prompt_slot_types_name
    on prompt_slot_types (name);

create table if not exists prompt_slot_variants
(
    id                     bigint                   default nextval('slots_id_seq'::regclass) not null,
    slot_type_id           bigint                                                             not null,
    name                   varchar(255)                                                       not null,
    prompt                 text,
    created_at             timestamp with time zone default CURRENT_TIMESTAMP                 not null,
    updated_at             timestamp with time zone default CURRENT_TIMESTAMP                 not null,
    description            text,
    example_image_filename varchar(500),
    prompt_slot_type_id    integer,
    constraint slots_pkey
        primary key (id),
    constraint slots_name_key
        unique (name),
    constraint fk_prompt_slot_variant_slot_type
        foreign key (slot_type_id) references prompt_slot_types
);

create index if not exists idx_prompt_slot_variants_name
    on prompt_slot_variants (name);

create index if not exists idx_prompt_slot_variants_slot_type_id
    on prompt_slot_variants (slot_type_id);

create table if not exists prompt_subcategories
(
    id                 bigserial,
    prompt_category_id bigint                                             not null,
    name               varchar(255)                                       not null,
    description        text,
    created_at         timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at         timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (id),
    constraint uk_prompt_subcategory_category_name
        unique (prompt_category_id, name),
    constraint fk_prompt_subcategories_category
        foreign key (prompt_category_id) references prompt_categories
            on delete cascade
);

create table if not exists prompts
(
    id                     bigserial,
    title                  varchar(500)                        not null,
    prompt_text            text,
    created_at             timestamp default CURRENT_TIMESTAMP not null,
    updated_at             timestamp default CURRENT_TIMESTAMP not null,
    category_id            bigint,
    active                 boolean   default true              not null,
    subcategory_id         bigint,
    example_image_filename varchar(500),
    primary key (id),
    constraint fk_prompts_category
        foreign key (category_id) references prompt_categories
            on delete set null,
    constraint fk_prompts_subcategory
        foreign key (subcategory_id) references prompt_subcategories
            on delete set null
);

create index if not exists idx_prompts_title
    on prompts (title);

create index if not exists idx_prompts_category_id
    on prompts (category_id);

create index if not exists idx_prompts_active
    on prompts (active);

create index if not exists idx_prompts_subcategory_id
    on prompts (subcategory_id);

create table if not exists prompt_slot_variant_mappings
(
    prompt_id  bigint                                             not null,
    slot_id    bigint                                             not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    constraint prompt_slots_pkey
        primary key (prompt_id, slot_id),
    constraint fk_prompt_slot_variant_mappings_prompt
        foreign key (prompt_id) references prompts
            on delete cascade,
    constraint fk_prompt_slot_variant_mappings_slot
        foreign key (slot_id) references prompt_slot_variants
            on delete cascade
);

create index if not exists idx_prompt_slot_variant_mappings_prompt_id
    on prompt_slot_variant_mappings (prompt_id);

create index if not exists idx_prompt_slot_variant_mappings_slot_id
    on prompt_slot_variant_mappings (slot_id);

create index if not exists idx_prompt_subcategories_category_id
    on prompt_subcategories (prompt_category_id);

create index if not exists idx_prompt_subcategories_name
    on prompt_subcategories (name);

create table if not exists roles
(
    id          bigserial,
    name        varchar(50)                                        not null,
    description varchar(255),
    created_at  timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP,
    primary key (id),
    unique (name)
);

create index if not exists idx_roles_name
    on roles (name);

create table if not exists user_roles
(
    user_id    bigint                                             not null,
    role_id    bigint                                             not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (user_id, role_id),
    constraint fk_user_roles_user
        foreign key (user_id) references users
            on delete cascade,
    constraint fk_user_roles_role
        foreign key (role_id) references roles
            on delete cascade
);

create index if not exists idx_user_roles_user_id
    on user_roles (user_id);

create index if not exists idx_user_roles_role_id
    on user_roles (role_id);

create table if not exists spring_session
(
    primary_id            char(36) not null,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100),
    constraint spring_session_pk
        primary key (primary_id)
);

create unique index if not exists spring_session_ix1
    on spring_session (session_id);

create index if not exists spring_session_ix2
    on spring_session (expiry_time);

create index if not exists spring_session_ix3
    on spring_session (principal_name);

create table if not exists spring_session_attributes
(
    session_primary_id char(36)     not null,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint spring_session_attributes_pk
        primary key (session_primary_id, attribute_name),
    constraint spring_session_attributes_fk
        foreign key (session_primary_id) references spring_session
            on delete cascade
);

create index if not exists spring_session_attributes_ix1
    on spring_session_attributes (session_primary_id);

create table if not exists value_added_taxes
(
    id          bigserial,
    name        varchar(255)                                       not null,
    percent     integer                                            not null,
    description text,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP not null,
    is_default  boolean                  default false             not null,
    primary key (id),
    unique (name),
    constraint value_added_taxes_percent_check
        check (percent > 0)
);

create index if not exists idx_value_added_taxes_name
    on value_added_taxes (name);

create index if not exists idx_value_added_taxes_is_default
    on value_added_taxes (is_default)
    where (is_default = true);

create table if not exists countries
(
    id         bigserial,
    name       varchar(255)                                       not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (id),
    unique (name)
);

create table if not exists suppliers
(
    id            bigserial,
    name          varchar(255),
    title         varchar(255),
    first_name    varchar(255),
    last_name     varchar(255),
    street        varchar(255),
    house_number  varchar(255),
    city          varchar(255),
    postal_code   integer,
    country       varchar(255),
    phone_number1 varchar(255),
    phone_number2 varchar(255),
    phone_number3 varchar(255),
    email         varchar(255),
    website       varchar(255),
    created_at    timestamp default CURRENT_TIMESTAMP not null,
    updated_at    timestamp default CURRENT_TIMESTAMP not null,
    country_id    bigint,
    primary key (id),
    constraint fk_supplier_country
        foreign key (country_id) references countries
);

create table if not exists articles
(
    id                      bigserial,
    name                    varchar(255) not null,
    description_short       text         not null,
    description_long        text         not null,
    active                  boolean                  default true,
    article_type            varchar(50)  not null,
    category_id             bigint       not null,
    subcategory_id          bigint,
    created_at              timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at              timestamp with time zone default CURRENT_TIMESTAMP,
    supplier_id             bigint,
    supplier_article_name   varchar(255),
    supplier_article_number varchar(255),
    primary key (id),
    foreign key (category_id) references article_categories,
    foreign key (subcategory_id) references article_sub_categories,
    constraint fk_articles_supplier
        foreign key (supplier_id) references suppliers
);

create index if not exists idx_articles_name
    on articles (name);

create index if not exists idx_articles_active
    on articles (active);

create index if not exists idx_articles_type
    on articles (article_type);

create index if not exists idx_articles_category_id
    on articles (category_id);

create index if not exists idx_articles_subcategory_id
    on articles (subcategory_id);

create index if not exists idx_articles_supplier_id
    on articles (supplier_id);

create index if not exists idx_articles_supplier_article_number
    on articles (supplier_article_number)
    where (supplier_article_number IS NOT NULL);

create table if not exists article_mug_details
(
    article_id                       bigint  not null,
    height_mm                        integer not null,
    diameter_mm                      integer not null,
    print_template_width_mm          integer not null,
    print_template_height_mm         integer not null,
    filling_quantity                 varchar(50),
    dishwasher_safe                  boolean                  default true,
    created_at                       timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at                       timestamp with time zone default CURRENT_TIMESTAMP,
    document_format_width_mm         integer,
    document_format_height_mm        integer,
    document_format_margin_bottom_mm integer,
    primary key (article_id),
    foreign key (article_id) references articles
        on delete cascade
);

create table if not exists article_shirt_details
(
    article_id        bigint       not null,
    material          varchar(255) not null,
    care_instructions text,
    fit_type          varchar(50)  not null,
    available_sizes   text[]       not null,
    created_at        timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at        timestamp with time zone default CURRENT_TIMESTAMP,
    primary key (article_id),
    foreign key (article_id) references articles
        on delete cascade
);

create table if not exists article_mug_variants
(
    id                     bigserial,
    article_id             bigint                                                        not null,
    inside_color_code      varchar(255)             default '#ffffff'::character varying not null,
    outside_color_code     varchar(255)             default '#ffffff'::character varying not null,
    name                   varchar(255)                                                  not null,
    example_image_filename varchar(500),
    created_at             timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at             timestamp with time zone default CURRENT_TIMESTAMP,
    article_variant_number varchar(100),
    is_default             boolean                  default false                        not null,
    active                 boolean                  default true                         not null,
    primary key (id),
    foreign key (article_id) references articles
        on delete cascade
);

create index if not exists idx_article_mug_variants_article_id
    on article_mug_variants (article_id);

create unique index if not exists idx_article_mug_variants_one_default_per_article
    on article_mug_variants (article_id)
    where (is_default = true);

create unique index if not exists idx_one_default_per_mug
    on article_mug_variants (article_id)
    where (is_default = true);

create index if not exists idx_article_mug_variants_article_variant_number
    on article_mug_variants (article_variant_number)
    where (article_variant_number IS NOT NULL);

create index if not exists idx_article_mug_variants_active
    on article_mug_variants (active);

create table if not exists article_shirt_variants
(
    id                     bigserial,
    article_id             bigint       not null,
    color                  varchar(255) not null,
    size                   varchar(50)  not null,
    example_image_filename varchar(500),
    created_at             timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at             timestamp with time zone default CURRENT_TIMESTAMP,
    primary key (id),
    foreign key (article_id) references articles
        on delete cascade
);

create index if not exists idx_article_shirt_variants_article_id
    on article_shirt_variants (article_id);

create index if not exists idx_suppliers_name
    on suppliers (name);

create index if not exists idx_suppliers_email
    on suppliers (email);

create index if not exists idx_suppliers_country_id
    on suppliers (country_id);

create index if not exists idx_countries_name
    on countries (name);

create table if not exists prices
(
    id                         bigint generated by default as identity,
    article_id                 bigint                                                          not null,
    purchase_price_net         integer                  default 0                              not null,
    purchase_price_tax         integer                  default 0                              not null,
    purchase_price_gross       integer                  default 0                              not null,
    purchase_cost_net          integer                  default 0                              not null,
    purchase_cost_tax          integer                  default 0                              not null,
    purchase_cost_gross        integer                  default 0                              not null,
    purchase_cost_percent      numeric(5, 2)            default 0                              not null,
    purchase_total_net         integer                  default 0                              not null,
    purchase_total_tax         integer                  default 0                              not null,
    purchase_total_gross       integer                  default 0                              not null,
    purchase_price_unit        varchar(50)              default 'PER_PIECE'::character varying not null,
    purchase_vat_rate_id       bigint,
    purchase_vat_rate_percent  numeric(5, 2)            default 19                             not null,
    purchase_calculation_mode  varchar(10)              default 'NET'::character varying       not null,
    sales_vat_rate_id          bigint,
    sales_vat_rate_percent     numeric(5, 2)            default 19                             not null,
    sales_margin_net           integer                  default 0                              not null,
    sales_margin_tax           integer                  default 0                              not null,
    sales_margin_gross         integer                  default 0                              not null,
    sales_margin_percent       numeric(5, 2)            default 0                              not null,
    sales_total_net            integer                  default 0                              not null,
    sales_total_tax            integer                  default 0                              not null,
    sales_total_gross          integer                  default 0                              not null,
    sales_price_unit           varchar(50)              default 'PER_PIECE'::character varying not null,
    sales_calculation_mode     varchar(10)              default 'NET'::character varying       not null,
    purchase_price_corresponds varchar(10)              default 'NET'::character varying       not null,
    sales_price_corresponds    varchar(10)              default 'NET'::character varying       not null,
    purchase_active_row        varchar(20)              default 'COST'::character varying      not null,
    sales_active_row           varchar(20)              default 'MARGIN'::character varying    not null,
    created_at                 timestamp with time zone default CURRENT_TIMESTAMP              not null,
    updated_at                 timestamp with time zone default CURRENT_TIMESTAMP              not null,
    constraint article_price_calculation_pkey
        primary key (id),
    constraint article_price_calculation_article_id_key
        unique (article_id),
    constraint fk_prices_article
        foreign key (article_id) references articles
            on delete cascade,
    constraint fk_prices_purchase_vat_rate
        foreign key (purchase_vat_rate_id) references value_added_taxes,
    constraint fk_prices_sales_vat_rate
        foreign key (sales_vat_rate_id) references value_added_taxes,
    constraint chk_purchase_calculation_mode
        check ((purchase_calculation_mode)::text = ANY
               ((ARRAY ['NET'::character varying, 'GROSS'::character varying])::text[])),
    constraint chk_sales_calculation_mode
        check ((sales_calculation_mode)::text = ANY
               ((ARRAY ['NET'::character varying, 'GROSS'::character varying])::text[])),
    constraint chk_purchase_price_corresponds
        check ((purchase_price_corresponds)::text = ANY
               ((ARRAY ['NET'::character varying, 'GROSS'::character varying])::text[])),
    constraint chk_sales_price_corresponds
        check ((sales_price_corresponds)::text = ANY
               ((ARRAY ['NET'::character varying, 'GROSS'::character varying])::text[])),
    constraint chk_purchase_active_row
        check ((purchase_active_row)::text = ANY
               ((ARRAY ['COST'::character varying, 'COST_PERCENT'::character varying])::text[])),
    constraint chk_sales_active_row
        check ((sales_active_row)::text = ANY
               ((ARRAY ['MARGIN'::character varying, 'MARGIN_PERCENT'::character varying, 'TOTAL'::character varying])::text[]))
);

create index if not exists idx_prices_article_id
    on prices (article_id);

create index if not exists idx_prices_purchase_vat_rate
    on prices (purchase_vat_rate_id);

create index if not exists idx_prices_sales_vat_rate
    on prices (sales_vat_rate_id);

create table if not exists uploaded_images
(
    id                bigserial,
    uuid              uuid                                               not null,
    original_filename varchar(255)                                       not null,
    stored_filename   varchar(255)                                       not null,
    content_type      varchar(100)                                       not null,
    file_size         bigint                                             not null,
    user_id           bigint                                             not null,
    created_at        timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (id),
    unique (uuid),
    unique (stored_filename),
    foreign key (user_id) references users,
    constraint fk_uploaded_images_user
        foreign key (user_id) references users
            on delete cascade
);

comment on column uploaded_images.created_at is 'Timestamp when the image was uploaded';

create table if not exists generated_images
(
    id                bigserial,
    filename          varchar(255)             not null,
    prompt_id         bigint                   not null,
    user_id           bigint,
    ip_address        varchar(45),
    created_at        timestamp with time zone not null,
    uploaded_image_id bigint,
    uuid              uuid                     not null,
    primary key (id),
    unique (filename),
    constraint uk_generated_images_uuid
        unique (uuid),
    foreign key (user_id) references users,
    constraint fk_generated_images_prompt
        foreign key (prompt_id) references prompts,
    constraint fk_generated_images_uploaded_image
        foreign key (uploaded_image_id) references uploaded_images
            on delete cascade
);

create index if not exists idx_generated_images_user_id
    on generated_images (user_id);

create index if not exists idx_generated_images_ip_address
    on generated_images (ip_address);

create index if not exists idx_generated_images_uploaded_image_id
    on generated_images (uploaded_image_id);

create index if not exists idx_generated_images_created_at
    on generated_images (created_at);

create index if not exists idx_generated_images_user_created
    on generated_images (user_id, created_at);

create index if not exists idx_generated_images_ip_created
    on generated_images (ip_address, created_at);

create index if not exists idx_uploaded_images_user_id
    on uploaded_images (user_id);

create index if not exists idx_uploaded_images_uuid
    on uploaded_images (uuid);

create index if not exists idx_uploaded_images_created_at
    on uploaded_images (created_at);

create table if not exists carts
(
    id         bigserial,
    user_id    bigint                                                       not null,
    status     varchar(20)              default 'active'::character varying not null,
    version    bigint                   default 0                           not null,
    expires_at timestamp with time zone,
    created_at timestamp with time zone default CURRENT_TIMESTAMP           not null,
    updated_at timestamp with time zone default CURRENT_TIMESTAMP           not null,
    primary key (id),
    foreign key (user_id) references users,
    constraint chk_cart_status
        check ((status)::text = ANY
               ((ARRAY ['active'::character varying, 'abandoned'::character varying, 'converted'::character varying])::text[]))
);

create index if not exists idx_carts_user_id
    on carts (user_id);

create index if not exists idx_carts_status_updated_at
    on carts (status, updated_at);

create unique index if not exists uk_user_active_cart
    on carts (user_id)
    where ((status)::text = 'active'::text);

create table if not exists cart_items
(
    id                 bigserial,
    cart_id            bigint                                             not null,
    article_id         bigint                                             not null,
    variant_id         bigint                                             not null,
    quantity           integer                                            not null,
    price_at_time      integer                                            not null,
    original_price     integer                                            not null,
    custom_data        jsonb                    default '{}'::jsonb       not null,
    position           integer                  default 0                 not null,
    created_at         timestamp with time zone default CURRENT_TIMESTAMP not null,
    updated_at         timestamp with time zone default CURRENT_TIMESTAMP not null,
    generated_image_id bigint,
    prompt_id          bigint,
    primary key (id),
    foreign key (cart_id) references carts
        on delete cascade,
    foreign key (article_id) references articles,
    foreign key (variant_id) references article_mug_variants,
    constraint fk_cart_items_generated_image_id
        foreign key (generated_image_id) references generated_images
            on delete set null,
    constraint fk_cart_items_prompt_id
        foreign key (prompt_id) references prompts
            on delete set null,
    constraint cart_items_quantity_check
        check (quantity > 0)
);

create index if not exists idx_cart_items_cart_id_position
    on cart_items (cart_id, position);

create index if not exists idx_cart_items_article_variant
    on cart_items (article_id, variant_id);

create index if not exists idx_cart_items_custom_data_gin
    on cart_items using gin (custom_data);

create unique index if not exists uk_cart_item_unique
    on cart_items (cart_id, article_id, variant_id, md5(custom_data::text));

create index if not exists idx_cart_items_generated_image_id
    on cart_items (generated_image_id);

create index if not exists idx_cart_items_prompt_id
    on cart_items (prompt_id);

create table if not exists orders
(
    id                        uuid                     default gen_random_uuid()                                       not null,
    order_number              varchar(50)              default ('ORD-'::text || nextval('order_number_seq'::regclass)) not null,
    user_id                   bigint                                                                                   not null,
    customer_email            varchar(255)                                                                             not null,
    customer_first_name       varchar(255)                                                                             not null,
    customer_last_name        varchar(255)                                                                             not null,
    customer_phone            varchar(50),
    shipping_street_address_1 varchar(255)                                                                             not null,
    shipping_street_address_2 varchar(255),
    shipping_city             varchar(100)                                                                             not null,
    shipping_state            varchar(100)                                                                             not null,
    shipping_postal_code      varchar(20)                                                                              not null,
    shipping_country          varchar(100)                                                                             not null,
    billing_street_address_1  varchar(255),
    billing_street_address_2  varchar(255),
    billing_city              varchar(100),
    billing_state             varchar(100),
    billing_postal_code       varchar(20),
    billing_country           varchar(100),
    subtotal                  bigint                                                                                   not null,
    tax_amount                bigint                                                                                   not null,
    shipping_amount           bigint                                                                                   not null,
    total_amount              bigint                                                                                   not null,
    status                    varchar(20)              default 'PENDING'::character varying                            not null,
    cart_id                   bigint                                                                                   not null,
    notes                     text,
    created_at                timestamp with time zone default CURRENT_TIMESTAMP                                       not null,
    updated_at                timestamp with time zone default CURRENT_TIMESTAMP                                       not null,
    primary key (id),
    unique (order_number),
    foreign key (user_id) references users,
    foreign key (cart_id) references carts,
    constraint chk_order_status
        check ((status)::text = ANY
               ((ARRAY ['PENDING'::character varying, 'PROCESSING'::character varying, 'SHIPPED'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[])),
    constraint chk_order_amounts_positive
        check ((subtotal >= 0) AND (tax_amount >= 0) AND (shipping_amount >= 0) AND (total_amount >= 0)),
    constraint chk_total_calculation
        check (total_amount = ((subtotal + tax_amount) + shipping_amount))
);

create index if not exists idx_orders_user_id
    on orders (user_id);

create index if not exists idx_orders_status
    on orders (status);

create index if not exists idx_orders_created_at
    on orders (created_at);

create index if not exists idx_orders_order_number
    on orders (order_number);

create index if not exists idx_orders_cart_id
    on orders (cart_id);

create table if not exists order_items
(
    id                 uuid                     default gen_random_uuid() not null,
    order_id           uuid                                               not null,
    article_id         bigint                                             not null,
    variant_id         bigint                                             not null,
    quantity           integer                                            not null,
    price_per_item     bigint                                             not null,
    total_price        bigint                                             not null,
    generated_image_id bigint,
    prompt_id          bigint,
    custom_data        jsonb                    default '{}'::jsonb       not null,
    created_at         timestamp with time zone default CURRENT_TIMESTAMP not null,
    primary key (id),
    foreign key (order_id) references orders
        on delete cascade,
    foreign key (article_id) references articles,
    foreign key (variant_id) references article_mug_variants,
    foreign key (prompt_id) references prompts,
    constraint order_items_quantity_check
        check (quantity > 0),
    constraint order_items_price_per_item_check
        check (price_per_item >= 0),
    constraint order_items_total_price_check
        check (total_price >= 0),
    constraint chk_order_item_total_price
        check (total_price = (price_per_item * quantity))
);

create index if not exists idx_order_items_order_id
    on order_items (order_id);

create index if not exists idx_order_items_article_variant
    on order_items (article_id, variant_id);

create index if not exists idx_order_items_created_at
    on order_items (created_at);

create index if not exists idx_order_items_custom_data_gin
    on order_items using gin (custom_data);

create table if not exists sessions
(
    id         varchar(128)                           not null,
    user_id    integer                                not null,
    created_at timestamp with time zone default now() not null,
    expires_at timestamp with time zone,
    primary key (id),
    foreign key (user_id) references users
);

create table if not exists schema_migrations
(
    version bigint  not null,
    dirty   boolean not null,
    primary key (version)
);
