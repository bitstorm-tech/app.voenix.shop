-- Add prompt price tracking columns to cart_items
alter table if exists cart_items
    add column if not exists prompt_price_at_time integer not null default 0;

alter table if exists cart_items
    add column if not exists prompt_original_price integer not null default 0;
