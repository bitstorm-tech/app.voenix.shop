-- Add indexes for User table performance optimization
-- These indexes improve query performance for email lookups and soft delete filtering
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);