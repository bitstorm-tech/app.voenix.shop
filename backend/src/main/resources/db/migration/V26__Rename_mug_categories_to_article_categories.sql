-- rename mug_categories table to article_categories
alter table mug_categories rename to article_categories;

-- rename indexes for article_categories
alter index idx_mug_categories_name rename to idx_article_categories_name;

-- rename mug_sub_categories table to article_sub_categories
alter table mug_sub_categories rename to article_sub_categories;

-- rename column mug_category_id to article_category_id
alter table article_sub_categories rename column mug_category_id to article_category_id;

-- rename constraint
alter table article_sub_categories rename constraint fk_mug_sub_categories_category to fk_article_sub_categories_category;

-- rename indexes for article_sub_categories
alter index idx_mug_sub_categories_category_id rename to idx_article_sub_categories_category_id;
alter index idx_mug_sub_categories_name rename to idx_article_sub_categories_name;
alter index idx_mug_sub_categories_category_name rename to idx_article_sub_categories_category_name;