-- Drop the composite index on prompt_id and position
drop index if exists idx_prompt_slots_prompt_position;

-- Drop the position column from prompt_slots table
alter table prompt_slots drop column position;