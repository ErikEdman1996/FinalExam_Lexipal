package com.lexipal.core.dtos.responses;

import java.time.LocalDateTime;

public record CodexBoardSlotResponseDTO(
        int slotNumber,
        boolean isCaptured,
        String lemma,
        String rarityTier,
        LocalDateTime capturedAt
)
{
}
