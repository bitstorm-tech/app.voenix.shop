-- Add supplier_id column to articles table
alter table articles
add column supplier_id bigint;



-- Add foreign key constraint
alter table articles
add constraint fk_articles_supplier foreign key (supplier_id) references suppliers (id);



-- Create index for better query performance
create index idx_articles_supplier_id on articles (supplier_id);