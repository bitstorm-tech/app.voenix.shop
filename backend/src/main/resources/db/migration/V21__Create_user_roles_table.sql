create table if not exists user_roles
(
    user_id    bigint      not null,
    role_id    bigint      not null,
    created_at timestamptz not null default current_timestamp,
    primary key (user_id, role_id),
    constraint fk_user_roles_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_user_roles_role foreign key (role_id) references roles (id) on delete cascade
);

create index if not exists idx_user_roles_user_id on user_roles (user_id);
create index if not exists idx_user_roles_role_id on user_roles (role_id);