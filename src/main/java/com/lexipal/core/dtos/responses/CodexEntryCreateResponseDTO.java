package com.lexipal.core.dtos.responses;

public class CodexEntryCreateResponseDTO
{
    private CodexEntryReadResponseDTO codexEntry;
    private boolean isNewCapture;
    private String rarityTier;

    public CodexEntryCreateResponseDTO()
    {

    }

    public CodexEntryCreateResponseDTO(CodexEntryReadResponseDTO codexEntry, boolean isNewCapture)
    {
        this.codexEntry = codexEntry;
        this.isNewCapture = isNewCapture;
        this.rarityTier = codexEntry.getRarityTier();
    }

    public CodexEntryReadResponseDTO getCodexEntry() {
        return codexEntry;
    }

    public void setCodexEntry(CodexEntryReadResponseDTO codexEntry) {
        this.codexEntry = codexEntry;
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
