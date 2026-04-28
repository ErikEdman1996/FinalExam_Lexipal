alter table books
    add column if not exists cover_image_url varchar(1000),
    add column if not exists cover_source varchar(50),
    add column if not exists external_book_id varchar(255);
