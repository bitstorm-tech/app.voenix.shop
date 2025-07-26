-- delete all pillow articles and their related data (cascading delete will handle related records)
delete from articles where article_type = 'PILLOW';

-- drop pillow-related tables
drop table if exists article_pillow_variants;
drop table if exists article_pillow_details;

-- note: we cannot remove 'PILLOW' from the article_type enum directly in postgresql
-- the application code will need to handle this by no longer using the PILLOW value
-- and any future migration could recreate the enum without PILLOW if needed