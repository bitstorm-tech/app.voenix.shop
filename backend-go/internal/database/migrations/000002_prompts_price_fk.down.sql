-- Revert prompts.price_id FK and make prices.article_id NOT NULL again

-- Drop unique and regular indices first (if they exist)
drop index if exists uq_prompts_price_id;
drop index if exists idx_prompts_price_id;

-- Drop FK then column
alter table if exists prompts
    drop constraint if exists fk_prompts_price;

alter table if exists prompts
    drop column if exists price_id;

-- Make prices.article_id NOT NULL again
-- Note: this will fail if rows exist with NULL article_id. Clean up before down migration.
alter table if exists prices
    alter column article_id set not null;

