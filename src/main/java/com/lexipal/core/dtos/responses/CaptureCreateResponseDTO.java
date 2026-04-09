package com.lexipal.core.dtos.responses;

public class CaptureCreateResponseDTO
{
    private CaptureReadResponseDTO capturedWord;
    private boolean isNewCapture;
    private String rarityTier;

    public CaptureCreateResponseDTO()
    {

    }

    public CaptureCreateResponseDTO(CaptureReadResponseDTO capturedWord, boolean isNewCapture)
    {
        this.capturedWord = capturedWord;
        this.isNewCapture = isNewCapture;
        this.rarityTier = capturedWord.getRarityTier();
    }

    public CaptureReadResponseDTO getCapturedWord() {
        return capturedWord;
    }

    public void setCapturedWord(CaptureReadResponseDTO capturedWord) {
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
