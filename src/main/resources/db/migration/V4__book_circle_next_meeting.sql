alter table book_circles
    add column if not exists next_meeting_date timestamp;

alter table book_circles
    add column if not exists meeting_chapter_label varchar(100);

alter table book_circles
    add column if not exists meeting_page_start integer;

alter table book_circles
    add column if not exists meeting_page_end integer;
