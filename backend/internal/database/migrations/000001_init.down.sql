-- Irreversible baseline migration
-- This file intentionally raises an exception to prevent accidental down.
do $$
BEGIN
  RAISE EXCEPTION 'Irreversible migration: baseline';
END $$;