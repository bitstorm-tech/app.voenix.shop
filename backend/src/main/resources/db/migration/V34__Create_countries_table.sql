-- Create countries table
create table countries (
    id bigserial primary key,
    name varchar(255) not null unique,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

-- Create index on name for faster lookups
create index idx_countries_name on countries(name);

-- Insert initial countries
insert into countries (name) values 
    ('Germany'),
    ('France'),
    ('Austria'),
    ('Poland'),
    ('Italy'),
    ('Switzerland');