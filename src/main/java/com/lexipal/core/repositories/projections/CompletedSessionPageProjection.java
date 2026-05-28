package com.lexipal.core.repositories.projections;

import java.time.LocalDateTime;

public interface CompletedSessionPageProjection
{
    LocalDateTime getCompletedAt();
    Integer getPagesRead();
}
