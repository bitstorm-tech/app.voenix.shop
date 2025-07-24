create table suppliers (
    id bigserial primary key,
    name varchar(255),
    title varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    street varchar(255),
    house_number varchar(255),
    city varchar(255),
    postal_code integer,
    country varchar(255),
    phone_number1 varchar(255),
    phone_number2 varchar(255),
    phone_number3 varchar(255),
    email varchar(255),
    website varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create index idx_suppliers_name on suppliers(name);
create index idx_suppliers_email on suppliers(email);