package com.lexipal.core.dtos.responses;

import java.util.List;

public record CodexBoardPageResponseDTO(
        int pageNumber,
        int capturedCount,
        int totalCount,
        double completionPercentage,
        List<CodexBoardSlotResponseDTO> slots
)
{
}
