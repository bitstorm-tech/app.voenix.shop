-- Rename table for broader usage beyond articles
alter table if exists article_price_calculation rename to prices;

-- Rename indexes to reflect new table name (if they exist)
alter index if exists idx_article_price_calculation_article_id rename to idx_prices_article_id;
alter index if exists idx_article_price_calculation_purchase_vat_rate rename to idx_prices_purchase_vat_rate;
alter index if exists idx_article_price_calculation_sales_vat_rate rename to idx_prices_sales_vat_rate;

-- Optionally rename foreign key constraints for clarity
do $$
begin
    if exists (
        select 1 from information_schema.table_constraints 
        where constraint_name = 'fk_cost_calculation_article'
    ) then
        alter table prices rename constraint fk_cost_calculation_article to fk_prices_article;
    end if;
    if exists (
        select 1 from information_schema.table_constraints 
        where constraint_name = 'fk_purchase_vat_rate'
    ) then
        alter table prices rename constraint fk_purchase_vat_rate to fk_prices_purchase_vat_rate;
    end if;
    if exists (
        select 1 from information_schema.table_constraints 
        where constraint_name = 'fk_sales_vat_rate'
    ) then
        alter table prices rename constraint fk_sales_vat_rate to fk_prices_sales_vat_rate;
    end if;
end $$;

