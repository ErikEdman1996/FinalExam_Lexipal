package com.lexipal.core.repositories.projections;

public interface AdminTopBookProjection
{
    String getTitle();
    String getAuthor();
    long getCompletedCount();
    long getPagesRead();
}
