create table prompt_categories
(
    id         bigserial primary key,
    name       varchar(255) not null unique,
    created_at timestamptz  not null default current_timestamp,
    updated_at timestamptz  not null default current_timestamp
);

create index idx_prompt_categories_name on prompt_categories (name);