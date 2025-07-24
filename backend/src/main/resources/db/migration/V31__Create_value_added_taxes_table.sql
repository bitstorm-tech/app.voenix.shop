-- Create value_added_taxes table
create table value_added_taxes (
    id bigserial primary key,
    name varchar(255) not null unique,
    percent integer not null check (percent > 0),
    description text,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

-- Create index on name for faster lookups
create index idx_value_added_taxes_name on value_added_taxes(name);