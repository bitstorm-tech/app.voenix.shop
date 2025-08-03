-- Add foreign key columns to cart_items table for better data structure
-- These columns will reference generated_images and prompts tables

-- Add nullable foreign key columns
ALTER TABLE cart_items
ADD COLUMN generated_image_id BIGINT NULL,
ADD COLUMN prompt_id BIGINT NULL;

-- Add foreign key constraints with ON DELETE SET NULL for data integrity
ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_generated_image_id
    FOREIGN KEY (generated_image_id)
    REFERENCES generated_images(id)
    ON DELETE SET NULL;

ALTER TABLE cart_items
ADD CONSTRAINT fk_cart_items_prompt_id
    FOREIGN KEY (prompt_id)
    REFERENCES prompts(id)
    ON DELETE SET NULL;

-- Create indexes for performance on foreign key columns
CREATE INDEX idx_cart_items_generated_image_id ON cart_items(generated_image_id);
CREATE INDEX idx_cart_items_prompt_id ON cart_items(prompt_id);