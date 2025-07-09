create table if not exists prompt_slots
(
    prompt_id  bigint      not null,
    slot_id    bigint      not null,
    position   int         not null default 0,
    created_at timestamptz not null default current_timestamp,
    primary key (prompt_id, slot_id),
    constraint fk_prompt_slots_prompt foreign key (prompt_id) references prompts (id) on delete cascade,
    constraint fk_prompt_slots_slot foreign key (slot_id) references slots (id) on delete cascade
);

create index if not exists idx_prompt_slots_prompt_id on prompt_slots (prompt_id);
create index if not exists idx_prompt_slots_slot_id on prompt_slots (slot_id);
create index if not exists idx_prompt_slots_position on prompt_slots (prompt_id, position);