create index if not exists idx_sessions_user_status_completed_at
    on sessions(user_id, status, completed_at)
    where completed_at is not null;
