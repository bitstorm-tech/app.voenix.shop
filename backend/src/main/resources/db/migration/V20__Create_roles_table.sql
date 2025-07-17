create table if not exists roles
(
    id          bigserial primary key,
    name        varchar(50) not null unique,
    description varchar(255),
    created_at  timestamptz not null default current_timestamp,
    updated_at  timestamptz          default current_timestamp
);

create index if not exists idx_roles_name on roles (name);