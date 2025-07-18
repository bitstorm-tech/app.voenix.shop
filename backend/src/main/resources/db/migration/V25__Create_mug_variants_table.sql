create table if not exists mug_variants
(
    id                      bigserial primary key,
    mug_id                  bigint       not null references mugs (id) on delete cascade,
    color_code              varchar(255) not null,
    example_image_filename  varchar(500) not null,
    created_at              timestamptz  not null default current_timestamp,
    updated_at              timestamptz  not null default current_timestamp
);

create index if not exists idx_mug_variants_mug_id on mug_variants (mug_id);