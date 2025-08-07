-- Add deleted_at column to users table for soft delete functionality
alter table users add column deleted_at timestamptz;

-- Create index for performance when filtering active users
create index idx_users_deleted_at on users (deleted_at);

-- Create partial index for active users (where deleted_at is null)
create index idx_users_active on users (id) where deleted_at is null;

-- Create partial unique index for active users email constraint
create unique index idx_users_active_email on users (email) where deleted_at is null;