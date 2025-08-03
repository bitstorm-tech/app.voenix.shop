-- Remove backward compatibility fields from cart_item custom_data JSON
-- Keep only cropData and similar non-FK fields

UPDATE cart_items 
SET custom_data = (
    SELECT jsonb_build_object(
        'cropData', custom_data->'cropData'
    )
    FROM (SELECT custom_data) AS cd
    WHERE cd.custom_data ? 'cropData'
)
WHERE custom_data ? 'cropData';

-- For items without cropData, set to empty JSON object
UPDATE cart_items 
SET custom_data = '{}'::jsonb
WHERE NOT (custom_data ? 'cropData');