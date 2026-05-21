package com.lexipal.core.dtos.responses;

import java.time.LocalDate;

public record StreakLeaderboardEntryResponseDTO(
        int rank,
        Long userId,
        String nickname,
        int currentStreak,
        LocalDate lastActivityDate
)
{
}
