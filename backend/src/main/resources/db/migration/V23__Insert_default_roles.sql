insert into roles (name, description)
values ('ADMIN', 'Administrator role with full access'),
       ('USER', 'Regular user role with limited access')
on conflict (name) do nothing;