package com.lexipal.core.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WordLookupCreateResponseDTO
{
    private WordLookupReadResponseDTO wordLookup;
    private CodexEntryReadResponseDTO codexEntry;
    private boolean lookupSucceeded;
    private boolean collectible;
    private boolean newLookup;
    private boolean newCapture;
    private boolean alreadyCaptured;
    private Integer pageNumber;
    private Integer slotNumber;
    private CodexBoardProgressResponseDTO pageProgress;
    private CodexBoardProgressResponseDTO overallProgress;
    private String rarityTier;
    private XpUpdateResponseDTO xpUpdate;

    public WordLookupCreateResponseDTO()
    {

    }

    public WordLookupCreateResponseDTO(WordLookupReadResponseDTO wordLookup,
                                       CodexEntryReadResponseDTO codexEntry,
                                       boolean lookupSucceeded,
                                       boolean collectible,
                                       boolean newLookup,
                                       boolean newCapture,
                                       boolean alreadyCaptured,
                                       Integer pageNumber,
                                       Integer slotNumber,
                                       CodexBoardProgressResponseDTO pageProgress,
                                       CodexBoardProgressResponseDTO overallProgress,
                                       String rarityTier,
                                       XpUpdateResponseDTO xpUpdate)
    {
        this.wordLookup = wordLookup;
        this.codexEntry = codexEntry;
        this.lookupSucceeded = lookupSucceeded;
        this.collectible = collectible;
        this.newLookup = newLookup;
        this.newCapture = newCapture;
        this.alreadyCaptured = alreadyCaptured;
        this.pageNumber = pageNumber;
        this.slotNumber = slotNumber;
        this.pageProgress = pageProgress;
        this.overallProgress = overallProgress;
        this.rarityTier = rarityTier;
        this.xpUpdate = xpUpdate;
    }

    public WordLookupReadResponseDTO getWordLookup()
    {
        return wordLookup;
    }

    public void setWordLookup(WordLookupReadResponseDTO wordLookup)
    {
        this.wordLookup = wordLookup;
    }

    public CodexEntryReadResponseDTO getCodexEntry()
    {
        return codexEntry;
    }

    public void setCodexEntry(CodexEntryReadResponseDTO codexEntry)
    {
        this.codexEntry = codexEntry;
    }

    public boolean isLookupSucceeded()
    {
        return lookupSucceeded;
    }

    public void setLookupSucceeded(boolean lookupSucceeded)
    {
        this.lookupSucceeded = lookupSucceeded;
    }

    @JsonProperty("isCollectible")
    public boolean isCollectible()
    {
        return collectible;
    }

    public void setCollectible(boolean collectible)
    {
        this.collectible = collectible;
    }

    @JsonProperty("isNewLookup")
    public boolean isNewLookup()
    {
        return newLookup;
    }

    public void setNewLookup(boolean newLookup)
    {
        this.newLookup = newLookup;
    }

    @JsonProperty("isNewCapture")
    public boolean isNewCapture()
    {
        return newCapture;
    }

    public void setNewCapture(boolean newCapture)
    {
        this.newCapture = newCapture;
    }

    public boolean isAlreadyCaptured()
    {
        return alreadyCaptured;
    }

    public void setAlreadyCaptured(boolean alreadyCaptured)
    {
        this.alreadyCaptured = alreadyCaptured;
    }

    public Integer getPageNumber()
    {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber)
    {
        this.pageNumber = pageNumber;
    }

    public Integer getSlotNumber()
    {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber)
    {
        this.slotNumber = slotNumber;
    }

    public CodexBoardProgressResponseDTO getPageProgress()
    {
        return pageProgress;
    }

    public void setPageProgress(CodexBoardProgressResponseDTO pageProgress)
    {
        this.pageProgress = pageProgress;
    }

    public CodexBoardProgressResponseDTO getOverallProgress()
    {
        return overallProgress;
    }

    public void setOverallProgress(CodexBoardProgressResponseDTO overallProgress)
    {
        this.overallProgress = overallProgress;
    }

    public String getRarityTier()
    {
        return rarityTier;
    }

    public void setRarityTier(String rarityTier)
    {
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
