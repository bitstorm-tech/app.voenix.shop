-- Rename slot_types table to prompt_slot_types
alter table slot_types rename to prompt_slot_types;

-- Rename slots table to prompt_slot_variants
alter table slots rename to prompt_slot_variants;

-- Rename prompt_slots table to prompt_slot_variant_mappings
alter table prompt_slots rename to prompt_slot_variant_mappings;

-- Update indexes for prompt_slot_types
alter index idx_slot_types_name rename to idx_prompt_slot_types_name;
alter index uk_slot_types_position rename to uk_prompt_slot_types_position;

-- Update indexes for prompt_slot_variants
alter index idx_slots_name rename to idx_prompt_slot_variants_name;
alter index idx_slots_slot_type_id rename to idx_prompt_slot_variants_slot_type_id;

-- Update indexes for prompt_slot_variant_mappings
alter index idx_prompt_slots_prompt_id rename to idx_prompt_slot_variant_mappings_prompt_id;
alter index idx_prompt_slots_slot_id rename to idx_prompt_slot_variant_mappings_slot_id;

-- Update constraint names for prompt_slot_variants
alter table prompt_slot_variants rename constraint fk_slot_slot_type to fk_prompt_slot_variant_slot_type;

-- Update constraint names for prompt_slot_variant_mappings
alter table prompt_slot_variant_mappings rename constraint fk_prompt_slots_prompt to fk_prompt_slot_variant_mappings_prompt;
alter table prompt_slot_variant_mappings rename constraint fk_prompt_slots_slot to fk_prompt_slot_variant_mappings_slot;