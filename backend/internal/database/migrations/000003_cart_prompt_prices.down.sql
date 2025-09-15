-- Remove prompt price tracking columns from cart_items
alter table if exists cart_items
    drop column prompt_price_at_time;

alter table if exists cart_items
    drop column prompt_original_price;
