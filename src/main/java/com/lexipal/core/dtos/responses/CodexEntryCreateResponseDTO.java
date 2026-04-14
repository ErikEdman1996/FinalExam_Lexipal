package com.lexipal.core.dtos.responses;

public class CodexEntryCreateResponseDTO
{
    private CodexEntryReadResponseDTO codexEntry;
    private boolean isNewCapture;
    private String rarityTier;
    private XpUpdateResponseDTO xpUpdate;

    public CodexEntryCreateResponseDTO()
    {

    }

    public CodexEntryCreateResponseDTO(CodexEntryReadResponseDTO codexEntry, boolean isNewCapture,
                                       XpUpdateResponseDTO xpUpdate)
    {
        this.codexEntry = codexEntry;
        this.isNewCapture = isNewCapture;
        this.rarityTier = codexEntry.getRarityTier();
        this.xpUpdate = xpUpdate;
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

    public XpUpdateResponseDTO getXpUpdate()
    {
        return xpUpdate;
    }

    public void setXpUpdate(XpUpdateResponseDTO xpUpdate)
    {
        this.xpUpdate = xpUpdate;
    }
}
