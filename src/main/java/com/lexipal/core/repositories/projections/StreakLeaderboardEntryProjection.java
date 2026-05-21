package com.lexipal.core.repositories.projections;

import java.time.LocalDate;

public interface StreakLeaderboardEntryProjection
{
    Long getUserId();
    String getNickname();
    Integer getCurrentStreak();
    LocalDate getLastActivityDate();
}
