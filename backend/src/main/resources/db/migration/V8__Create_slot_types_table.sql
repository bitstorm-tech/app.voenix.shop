create table if not exists slot_types
(
    id         bigserial primary key,
    name       varchar(255) not null unique,
    created_at timestamptz  not null default current_timestamp,
    updated_at timestamptz  not null default current_timestamp
);

create index if not exists idx_slot_types_name on slot_types (name);