package com.lexipal.core.dtos.responses;

import com.lexipal.core.entities.CapturedWord;

public class CaptureCreateResponseDTO
{
    private CapturedWord capturedWord;
    private boolean isNewCapture;
    private String rarityTier;

    public CaptureCreateResponseDTO()
    {

    }

    public CaptureCreateResponseDTO(CapturedWord capturedWord, boolean isNewCapture)
    {
        this.capturedWord = capturedWord;
        this.isNewCapture = isNewCapture;
        this.rarityTier = determineTier(capturedWord.getWord().getRarityScore());
    }

    private String determineTier(Double score)
    {
        if (score >= 0.9) return "MYTHIC";  // Purple/Gold borders
        if (score >= 0.7) return "RARE";    // Blue borders
        return "COMMON";                    // Standard borders
    }

    public CapturedWord getCapturedWord() {
        return capturedWord;
    }

    public void setCapturedWord(CapturedWord capturedWord) {
        this.capturedWord = capturedWord;
    }

    public boolean isNewCapture() {
        return isNewCapture;
    }

    public void setNewCapture(boolean newCapture) {
        isNewCapture = newCapture;
    }

    public String getRarityTier() {
        return rarityTier;
    }

    public void setRarityTier(String rarityTier) {
        this.rarityTier = rarityTier;
    }
}
