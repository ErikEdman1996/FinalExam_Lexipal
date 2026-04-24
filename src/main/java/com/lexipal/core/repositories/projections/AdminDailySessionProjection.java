package com.lexipal.core.repositories.projections;

public interface AdminDailySessionProjection
{
    Object getDate();
    long getPagesRead();
    long getCompletedSessions();
    long getActiveUsers();
}
