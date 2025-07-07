create table if not exists users
(
    id                           bigserial primary key,
    email                        varchar(255) not null unique,
    first_name                   varchar(255),
    last_name                    varchar(255),
    phone_number                 varchar(255),
    password                     varchar(255),
    one_time_password            varchar(255),
    one_time_password_created_at timestamptz,
    created_at                   timestamptz  not null default current_timestamp,
    updated_at                   timestamptz           default current_timestamp
);

create index if not exists idx_users_email on users (email);