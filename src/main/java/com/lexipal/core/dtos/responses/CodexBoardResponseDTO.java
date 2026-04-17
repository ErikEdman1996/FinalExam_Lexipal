package com.lexipal.core.dtos.responses;

import java.util.List;

public record CodexBoardResponseDTO(
        String volume,
        int totalWords,
        int capturedWords,
        double completionPercentage,
        List<CodexBoardPageResponseDTO> pages
)
{
}
