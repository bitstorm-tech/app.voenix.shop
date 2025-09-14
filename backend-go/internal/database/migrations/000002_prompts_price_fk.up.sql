-- Add price_id to prompts and relax prices.article_id for prompt-linked prices

-- 1) Extend prompts with nullable FK to prices
alter table if exists prompts
    add column if not exists price_id bigint;

alter table if exists prompts
    add constraint fk_prompts_price
        foreign key (price_id) references prices(id)
            on delete set null;

create index if not exists idx_prompts_price_id
    on prompts (price_id);

-- Ensure a price row is not reused across multiple prompts (optional but helpful)
-- Partial unique index only when price_id is not null
create unique index if not exists uq_prompts_price_id
    on prompts (price_id)
    where price_id is not null;

-- 2) Allow prices rows to exist without an article when used by prompts
alter table if exists prices
    alter column article_id drop not null;

