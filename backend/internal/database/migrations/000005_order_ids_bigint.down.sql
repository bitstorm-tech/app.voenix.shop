-- Revert orders and order_items identifiers back to UUIDs with database defaults

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Introduce UUID columns that will replace bigint identifiers
ALTER TABLE orders
    ADD COLUMN id_uuid UUID DEFAULT gen_random_uuid();

UPDATE orders SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;
ALTER TABLE orders ALTER COLUMN id_uuid SET NOT NULL;

ALTER TABLE order_items
    ADD COLUMN id_uuid UUID DEFAULT gen_random_uuid();

ALTER TABLE order_items
    ADD COLUMN order_id_uuid UUID;

UPDATE order_items SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;

UPDATE order_items
SET order_id_uuid = o.id_uuid
FROM orders o
WHERE order_items.order_id = o.id AND order_items.order_id_uuid IS NULL;

ALTER TABLE order_items ALTER COLUMN order_id_uuid SET NOT NULL;

-- Drop constraints tied to bigint identifiers before swapping columns
ALTER TABLE order_items DROP CONSTRAINT order_items_pkey;
ALTER TABLE order_items DROP CONSTRAINT order_items_order_id_fkey;
ALTER TABLE orders DROP CONSTRAINT orders_pkey;

DROP INDEX IF EXISTS idx_order_items_order_id;

-- Remove bigint defaults so sequences can be dropped cleanly
ALTER TABLE orders ALTER COLUMN id DROP DEFAULT;
ALTER TABLE order_items ALTER COLUMN id DROP DEFAULT;

-- Swap UUID columns into place
ALTER TABLE orders RENAME COLUMN id TO id_int;
ALTER TABLE orders RENAME COLUMN id_uuid TO id;

ALTER TABLE order_items RENAME COLUMN id TO id_int;
ALTER TABLE order_items RENAME COLUMN id_uuid TO id;
ALTER TABLE order_items RENAME COLUMN order_id TO order_id_int;
ALTER TABLE order_items RENAME COLUMN order_id_uuid TO order_id;

-- Restore primary keys and foreign keys on UUID identifiers
ALTER TABLE orders ADD CONSTRAINT orders_pkey PRIMARY KEY (id);
ALTER TABLE order_items ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);
ALTER TABLE order_items
    ADD CONSTRAINT order_items_order_id_fkey
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Clean up bigint columns and sequences
ALTER TABLE order_items DROP COLUMN order_id_int;
ALTER TABLE order_items DROP COLUMN id_int;
ALTER TABLE orders DROP COLUMN id_int;

DROP SEQUENCE IF EXISTS orders_id_seq;
DROP SEQUENCE IF EXISTS orders_id_int_seq;
DROP SEQUENCE IF EXISTS order_items_id_seq;
DROP SEQUENCE IF EXISTS order_items_id_int_seq;
