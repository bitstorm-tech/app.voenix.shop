alter table prompt_slot_variants
    add column if not exists example_image_filename varchar(500);
