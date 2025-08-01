-- Create uploaded_images table to track user-uploaded images
create table uploaded_images (
    id bigserial primary key,
    uuid uuid not null unique,
    original_filename varchar(255) not null,
    stored_filename varchar(255) not null unique,
    content_type varchar(100) not null,
    file_size bigint not null,
    user_id bigint not null references users(id),
    uploaded_at timestamp not null default current_timestamp,
    constraint fk_uploaded_images_user foreign key (user_id) references users(id) on delete cascade
);

-- Add uploaded_image_id to generated_images table to link generated images to their original upload
alter table generated_images add column uploaded_image_id bigint;

-- Add foreign key constraint
alter table generated_images add constraint fk_generated_images_uploaded_image 
    foreign key (uploaded_image_id) references uploaded_images(id) on delete cascade;

-- Create indexes for performance
create index idx_uploaded_images_user_id on uploaded_images(user_id);
create index idx_uploaded_images_uuid on uploaded_images(uuid);
create index idx_uploaded_images_uploaded_at on uploaded_images(uploaded_at);
create index idx_generated_images_uploaded_image_id on generated_images(uploaded_image_id);
