-- Set custom starting values for ID sequences
-- This migration only sets the sequences for future inserts, it does not update existing data
-- to avoid foreign key constraint violations

-- Check if we have existing data that would conflict with our new ID ranges
DO $$
DECLARE
    max_article_category_id bigint;
    max_prompt_category_id bigint;
    max_article_subcategory_id bigint;
    max_prompt_subcategory_id bigint;
    max_article_id bigint;
    max_prompt_id bigint;
BEGIN
    -- Get maximum IDs
    SELECT COALESCE(MAX(id), 0) INTO max_article_category_id FROM article_categories;
    SELECT COALESCE(MAX(id), 0) INTO max_prompt_category_id FROM prompt_categories;
    SELECT COALESCE(MAX(id), 0) INTO max_article_subcategory_id FROM article_sub_categories;
    SELECT COALESCE(MAX(id), 0) INTO max_prompt_subcategory_id FROM prompt_subcategories;
    SELECT COALESCE(MAX(id), 0) INTO max_article_id FROM articles;
    SELECT COALESCE(MAX(id), 0) INTO max_prompt_id FROM prompts;
    
    -- Raise notice if existing data would conflict
    IF max_article_category_id >= 101 THEN
        RAISE NOTICE 'Article categories already have IDs >= 101';
    END IF;
    IF max_prompt_category_id >= 101 THEN
        RAISE NOTICE 'Prompt categories already have IDs >= 101';
    END IF;
    IF max_article_subcategory_id >= 1001 THEN
        RAISE NOTICE 'Article subcategories already have IDs >= 1001';
    END IF;
    IF max_prompt_subcategory_id >= 1001 THEN
        RAISE NOTICE 'Prompt subcategories already have IDs >= 1001';
    END IF;
    IF max_article_id >= 100000 THEN
        RAISE NOTICE 'Articles already have IDs >= 100000';
    END IF;
    IF max_prompt_id >= 100000 THEN
        RAISE NOTICE 'Prompts already have IDs >= 100000';
    END IF;
END $$;

-- Set sequences to start at desired values or continue from current max if higher
-- Article Categories - start at 101 (sequence is still named mug_categories_id_seq after rename)
SELECT setval('mug_categories_id_seq', GREATEST(101, (SELECT COALESCE(MAX(id), 0) + 1 FROM article_categories)), false);

-- Prompt Categories - start at 101  
SELECT setval('prompt_categories_id_seq', GREATEST(101, (SELECT COALESCE(MAX(id), 0) + 1 FROM prompt_categories)), false);

-- Article Subcategories - start at 1001 (sequence is still named mug_sub_categories_id_seq after rename)
SELECT setval('mug_sub_categories_id_seq', GREATEST(1001, (SELECT COALESCE(MAX(id), 0) + 1 FROM article_sub_categories)), false);

-- Prompt Subcategories - start at 1001
SELECT setval('prompt_subcategories_id_seq', GREATEST(1001, (SELECT COALESCE(MAX(id), 0) + 1 FROM prompt_subcategories)), false);

-- Articles - start at 100000
SELECT setval('articles_id_seq', GREATEST(100000, (SELECT COALESCE(MAX(id), 0) + 1 FROM articles)), false);

-- Prompts - start at 100000
SELECT setval('prompts_id_seq', GREATEST(100000, (SELECT COALESCE(MAX(id), 0) + 1 FROM prompts)), false);