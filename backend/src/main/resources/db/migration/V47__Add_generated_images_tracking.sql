create table generated_images (
    id bigserial primary key,
    filename varchar(255) not null unique,
    prompt_id bigint not null,
    user_id bigint references users(id),
    ip_address varchar(45),
    generated_at timestamp not null,
    constraint fk_generated_images_prompt foreign key (prompt_id) references prompts(id)
);

create index idx_generated_images_user_id on generated_images(user_id);
create index idx_generated_images_ip_address on generated_images(ip_address);
create index idx_generated_images_generated_at on generated_images(generated_at);
create index idx_generated_images_user_generated on generated_images(user_id, generated_at);
create index idx_generated_images_ip_generated on generated_images(ip_address, generated_at);

comment on table generated_images is 'Tracks all AI-generated images with user/IP association for rate limiting and analytics';
comment on column generated_images.filename is 'Unique filename of the generated image';
comment on column generated_images.prompt_id is 'ID of the prompt used to generate the image';
comment on column generated_images.user_id is 'ID of the authenticated user who generated the image (null for anonymous)';
comment on column generated_images.ip_address is 'IP address of the client (for rate limiting anonymous users)';
comment on column generated_images.generated_at is 'Timestamp when the image was generated';