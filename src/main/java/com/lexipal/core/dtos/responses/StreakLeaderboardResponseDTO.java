package com.lexipal.core.dtos.responses;

import java.time.LocalDate;
import java.util.List;

public record StreakLeaderboardResponseDTO(
        int limit,
        LocalDate asOfDate,
        String timezone,
        List<StreakLeaderboardEntryResponseDTO> entries
)
{
}
