-- Drop example_image_filename and price columns from articles table
alter table articles drop column if exists example_image_filename;
alter table articles drop column if exists price;