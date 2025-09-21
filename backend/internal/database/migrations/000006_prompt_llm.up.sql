alter table if exists prompts
    add column if not exists llm varchar(255);
