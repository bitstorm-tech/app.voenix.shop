create table if not exists prompt_subcategories
(
    id                 bigserial primary key,
    prompt_category_id bigint       not null,
    name               varchar(255) not null,
    description        text,
    created_at         timestamptz  not null default current_timestamp,
    updated_at         timestamptz  not null default current_timestamp,
    constraint fk_prompt_subcategories_category
        foreign key (prompt_category_id) references prompt_categories (id)
            on delete cascade,
    constraint uk_prompt_subcategory_category_name unique (prompt_category_id, name)
);

create index if not exists idx_prompt_subcategories_category_id on prompt_subcategories (prompt_category_id);
create index if not exists idx_prompt_subcategories_name on prompt_subcategories (name);