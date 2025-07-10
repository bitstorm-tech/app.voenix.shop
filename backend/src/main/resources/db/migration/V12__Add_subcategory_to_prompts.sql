-- add subcategory_id column to prompts table
alter table prompts
    add column subcategory_id bigint;

-- add foreign key constraint
alter table prompts
    add constraint fk_prompts_subcategory
        foreign key (subcategory_id) references prompt_subcategories (id)
            on delete set null;

-- create index for better query performance
create index idx_prompts_subcategory_id on prompts (subcategory_id);