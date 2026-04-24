alter table books add column if not exists completed_at timestamp;

update books b
set completed_at = coalesce(
    (
        select max(s.completed_at)
        from sessions s
        where s.book_id = b.id
          and s.status = 'COMPLETED'
    ),
    b.created_at
)
where b.status = 'COMPLETE'
  and b.completed_at is null;

create index if not exists idx_sessions_status_completed_at on sessions(status, completed_at);
create index if not exists idx_books_status_completed_at on books(status, completed_at);
create index if not exists idx_users_created_at on users(created_at);
create index if not exists idx_daily_activities_date_type on daily_activities(activity_date, activity_type);
create index if not exists idx_reading_list_subscriptions_subscribed_completed on reading_list_subscriptions(subscribed_at, completed_at);
