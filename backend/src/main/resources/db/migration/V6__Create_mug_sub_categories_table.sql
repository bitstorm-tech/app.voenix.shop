create table if not exists mug_sub_categories
(
    id              bigserial primary key,
    mug_category_id bigint       not null,
    name            varchar(255) not null,
    description     text,
    created_at      timestamptz  not null default current_timestamp,
    updated_at      timestamptz  not null default current_timestamp,
    constraint fk_mug_sub_categories_category foreign key (mug_category_id) references mug_categories (id)
);

create index if not exists idx_mug_sub_categories_category_id on mug_sub_categories (mug_category_id);
create index if not exists idx_mug_sub_categories_name on mug_sub_categories (name);
create unique index if not exists idx_mug_sub_categories_category_name on mug_sub_categories (mug_category_id, lower(name));