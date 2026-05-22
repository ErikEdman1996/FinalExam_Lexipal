package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public record ThemeResponseDTO(
        String code,
        String name,
        String description,
        String tone,
        Integer sortOrder,
        boolean unlocked,
        boolean selected,
        Integer priceCoins,
        LocalDateTime unlockedAt
)
{
}
