create table if not exists prompts
(
    id         bigserial primary key,
    title      varchar(500) not null,
    content    text,
    created_at timestamp    not null default current_timestamp,
    updated_at timestamp    not null default current_timestamp
);

create index if not exists idx_prompts_title on prompts (title);