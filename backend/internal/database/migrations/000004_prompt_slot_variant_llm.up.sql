alter table prompt_slot_variants
    add column if not exists llm varchar(255);
