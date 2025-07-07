create table if not exists mug_categories
(
    id          bigserial primary key,
    name        varchar(255) not null,
    description text,
    created_at  timestamptz  not null default current_timestamp,
    updated_at  timestamptz  not null default current_timestamp
);

create index if not exists idx_mug_categories_name on mug_categories (name);