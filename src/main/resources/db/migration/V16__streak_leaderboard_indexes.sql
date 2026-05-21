create index if not exists idx_streak_metadata_leaderboard
    on streak_metadata(current_streak desc, last_activity_date desc, user_id);

create index if not exists idx_users_leaderboard_status_nickname
    on users(status, nickname);
