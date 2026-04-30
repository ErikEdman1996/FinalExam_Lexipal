drop table if exists story_unique_views;
drop table if exists story_chapter_views;
drop table if exists story_subscriptions;
drop table if exists story_likes;
drop table if exists story_tags;
drop table if exists story_genres;
drop table if exists story_chapters;
drop table if exists stories;

alter table users drop column if exists adult_content_enabled;
