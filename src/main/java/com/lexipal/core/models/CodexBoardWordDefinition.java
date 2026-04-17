package com.lexipal.core.models;

public record CodexBoardWordDefinition(
        String volume,
        int pageNumber,
        int slotNumber,
        String lemma,
        String rarityTier,
        double rarityScore
)
{
}
