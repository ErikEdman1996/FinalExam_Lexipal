package com.lexipal.core.dtos.responses;

public class BookCoverCandidateResponseDTO
{
    private String coverImageUrl;
    private String coverSource;
    private String externalBookId;
    private String matchedTitle;
    private String matchedAuthor;
    private int confidence;

    public BookCoverCandidateResponseDTO()
    {

    }

    public BookCoverCandidateResponseDTO(String coverImageUrl,
                                         String coverSource,
                                         String externalBookId,
                                         String matchedTitle,
                                         String matchedAuthor,
                                         int confidence)
    {
        this.coverImageUrl = coverImageUrl;
        this.coverSource = coverSource;
        this.externalBookId = externalBookId;
        this.matchedTitle = matchedTitle;
        this.matchedAuthor = matchedAuthor;
        this.confidence = confidence;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getCoverSource() {
        return coverSource;
    }

    public void setCoverSource(String coverSource) {
        this.coverSource = coverSource;
    }

    public String getExternalBookId() {
        return externalBookId;
    }

    public void setExternalBookId(String externalBookId) {
        this.externalBookId = externalBookId;
    }

    public String getMatchedTitle() {
        return matchedTitle;
    }

    public void setMatchedTitle(String matchedTitle) {
        this.matchedTitle = matchedTitle;
    }

    public String getMatchedAuthor() {
        return matchedAuthor;
    }

    public void setMatchedAuthor(String matchedAuthor) {
        this.matchedAuthor = matchedAuthor;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }
}
