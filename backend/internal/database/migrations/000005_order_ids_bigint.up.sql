-- Migrate orders and order_items identifiers from UUID to bigint sequences

-- Ensure pgcrypto extension exists for legacy defaults (no-op if already present)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Add new bigint identifiers to orders
ALTER TABLE orders
    ADD COLUMN id_int BIGINT;

CREATE SEQUENCE IF NOT EXISTS orders_id_int_seq OWNED BY orders.id_int;
ALTER TABLE orders ALTER COLUMN id_int SET DEFAULT nextval('orders_id_int_seq');

UPDATE orders SET id_int = nextval('orders_id_int_seq') WHERE id_int IS NULL;
ALTER TABLE orders ALTER COLUMN id_int SET NOT NULL;

-- Prepare order_items for new identifiers
ALTER TABLE order_items
    ADD COLUMN id_int BIGINT,
    ADD COLUMN order_id_int BIGINT;

CREATE SEQUENCE IF NOT EXISTS order_items_id_int_seq OWNED BY order_items.id_int;
ALTER TABLE order_items ALTER COLUMN id_int SET DEFAULT nextval('order_items_id_int_seq');

UPDATE order_items SET id_int = nextval('order_items_id_int_seq') WHERE id_int IS NULL;
ALTER TABLE order_items ALTER COLUMN id_int SET NOT NULL;

UPDATE order_items
SET order_id_int = o.id_int
FROM orders o
WHERE order_items.order_id = o.id AND order_items.order_id_int IS NULL;

ALTER TABLE order_items ALTER COLUMN order_id_int SET NOT NULL;

-- Drop constraints relying on UUID identifiers
ALTER TABLE order_items DROP CONSTRAINT order_items_pkey;
ALTER TABLE order_items DROP CONSTRAINT order_items_order_id_fkey;
ALTER TABLE orders DROP CONSTRAINT orders_pkey;

DROP INDEX IF EXISTS idx_order_items_order_id;

-- Rename columns to swap in bigint identifiers
ALTER TABLE orders RENAME COLUMN id TO id_uuid;
ALTER TABLE orders RENAME COLUMN id_int TO id;

ALTER TABLE order_items RENAME COLUMN id TO id_uuid;
ALTER TABLE order_items RENAME COLUMN id_int TO id;
ALTER TABLE order_items RENAME COLUMN order_id TO order_id_uuid;
ALTER TABLE order_items RENAME COLUMN order_id_int TO order_id;

-- Recreate primary keys and constraints on bigint identifiers
ALTER TABLE orders ADD CONSTRAINT orders_pkey PRIMARY KEY (id);
ALTER TABLE order_items ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);
ALTER TABLE order_items
    ADD CONSTRAINT order_items_order_id_fkey
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;

-- Recreate supporting index on the new order_id column
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Drop legacy UUID columns now that bigint identifiers are in place
ALTER TABLE order_items DROP COLUMN order_id_uuid;
ALTER TABLE order_items DROP COLUMN id_uuid;
ALTER TABLE orders DROP COLUMN id_uuid;

-- Align sequence ownership and positions with the new identifiers
ALTER SEQUENCE orders_id_int_seq OWNED BY orders.id;
ALTER SEQUENCE order_items_id_int_seq OWNED BY order_items.id;

ALTER SEQUENCE orders_id_int_seq RENAME TO orders_id_seq;
ALTER SEQUENCE order_items_id_int_seq RENAME TO order_items_id_seq;

SELECT setval('orders_id_seq', (SELECT COALESCE(MAX(id), 0) FROM orders));
SELECT setval('order_items_id_seq', (SELECT COALESCE(MAX(id), 0) FROM order_items));
