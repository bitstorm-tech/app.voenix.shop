-- update existing data to match new enum values
update article_price_calculation set purchase_active_row = 'COST' where purchase_active_row = 'PRICE';

-- drop old constraints
alter table article_price_calculation drop constraint chk_purchase_active_row;
alter table article_price_calculation drop constraint chk_sales_active_row;

-- add new constraints with updated enum values
alter table article_price_calculation add constraint chk_purchase_active_row check (purchase_active_row in ('COST', 'COST_PERCENT'));
alter table article_price_calculation add constraint chk_sales_active_row check (sales_active_row in ('MARGIN', 'MARGIN_PERCENT', 'TOTAL'));

-- update default value for purchase_active_row
alter table article_price_calculation alter column purchase_active_row set default 'COST';