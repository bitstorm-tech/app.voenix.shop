-- Add country_id column to suppliers table
alter table suppliers add column country_id bigint;

-- Add foreign key constraint
alter table suppliers add constraint fk_supplier_country 
    foreign key (country_id) references countries(id);

-- Create index for better performance
create index idx_suppliers_country_id on suppliers(country_id);

-- Migrate existing country data to country_id
-- This will attempt to match country names with the countries table
update suppliers s
set country_id = c.id
from countries c
where lower(trim(s.country)) = lower(trim(c.name))
and s.country is not null;

-- After migration is complete and verified, the country column can be dropped
-- This should be done in a separate migration after verifying data integrity
-- alter table suppliers drop column country;