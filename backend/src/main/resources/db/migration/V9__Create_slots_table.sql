create table if not exists slots
(
    id           bigserial primary key,
    slot_type_id bigint       not null,
    name         varchar(255) not null unique,
    prompt       text         not null,
    created_at   timestamptz  not null default current_timestamp,
    updated_at   timestamptz  not null default current_timestamp,
    constraint fk_slot_slot_type foreign key (slot_type_id) references slot_types (id)
);

create index if not exists idx_slots_name on slots (name);
create index if not exists idx_slots_slot_type_id on slots (slot_type_id);