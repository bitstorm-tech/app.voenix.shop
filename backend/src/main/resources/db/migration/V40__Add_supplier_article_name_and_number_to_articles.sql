-- add supplier_article_name and supplier_article_number columns to articles table
alter table articles 
    add column supplier_article_name varchar(255),
    add column supplier_article_number varchar(255);

-- add index for supplier article number for faster lookups
create index idx_articles_supplier_article_number 
on articles (supplier_article_number) 
where supplier_article_number is not null;