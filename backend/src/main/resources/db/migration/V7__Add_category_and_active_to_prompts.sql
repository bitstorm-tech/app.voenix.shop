-- add category_id column to prompts table
alter table prompts
    add column category_id bigint;

-- add active column to prompts table with default true
alter table prompts
    add column active boolean not null default true;

-- add foreign key constraint
alter table prompts
    add constraint fk_prompts_category
        foreign key (category_id) references prompt_categories (id)
            on delete set null;

-- create index for better query performance
create index idx_prompts_category_id on prompts (category_id);
create index idx_prompts_active on prompts (active);