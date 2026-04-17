package com.lexipal.core.dtos.responses;

public record CodexBoardProgressResponseDTO(
        int capturedWords,
        int totalWords,
        double completionPercentage
)
{
}
