package com.lexipal.core.dtos.responses;

public class ReadingListSlotResponseDTO
{
    private Long catalogBookId;
    private Integer position;
    private String title;
    private String author;
    private String note;
    private Boolean inMyLibrary;
    private Boolean completedByMe;

    public ReadingListSlotResponseDTO()
    {

    }

    public ReadingListSlotResponseDTO(Long catalogBookId, Integer position, String title, String author,
                                      String note, Boolean inMyLibrary, Boolean completedByMe)
    {
        this.catalogBookId = catalogBookId;
        this.position = position;
        this.title = title;
        this.author = author;
        this.note = note;
        this.inMyLibrary = inMyLibrary;
        this.completedByMe = completedByMe;
    }

    public Long getCatalogBookId() { return catalogBookId; }
    public void setCatalogBookId(Long catalogBookId) { this.catalogBookId = catalogBookId; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getInMyLibrary() { return inMyLibrary; }
    public void setInMyLibrary(Boolean inMyLibrary) { this.inMyLibrary = inMyLibrary; }

    public Boolean getCompletedByMe() { return completedByMe; }
    public void setCompletedByMe(Boolean completedByMe) { this.completedByMe = completedByMe; }
}
