package com.lexipal.core.repositories.projections;

public interface AdminTopUserProjection
{
    long getUserId();
    String getNickname();
    long getTotalPagesRead();
    long getCompletedBooks();
    long getCompletedSessions();
    long getTotalXpEarned();
}
