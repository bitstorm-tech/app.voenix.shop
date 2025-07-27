-- Add is_default column to value_added_taxes table
alter table value_added_taxes
    add column is_default boolean not null default false;

-- Add index for faster lookup of default VAT
create index idx_value_added_taxes_is_default on value_added_taxes(is_default) where is_default = true;