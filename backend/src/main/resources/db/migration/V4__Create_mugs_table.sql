create table if not exists mugs
(
    id                       bigserial primary key,
    name                     varchar(255) not null,
    description_long         text         not null,
    description_short        text         not null,
    image                    varchar(500) not null,
    price                    integer      not null,
    height_mm                integer      not null,
    diameter_mm              integer      not null,
    print_template_width_mm  integer      not null,
    print_template_height_mm integer      not null,
    filling_quantity         varchar(255),
    dishwasher_safe          boolean      not null default true,
    active                   boolean      not null default true,
    created_at               timestamptz  not null default current_timestamp,
    updated_at               timestamptz  not null default current_timestamp
);

create index if not exists idx_mugs_name on mugs (name);
create index if not exists idx_mugs_active on mugs (active);