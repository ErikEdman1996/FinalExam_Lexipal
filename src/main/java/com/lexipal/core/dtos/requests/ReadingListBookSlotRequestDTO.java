package com.lexipal.core.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReadingListBookSlotRequestDTO
{
    @NotBlank(message = "Slot title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotBlank(message = "Slot author is required")
    @Size(max = 200, message = "Author must be 200 characters or fewer")
    private String author;

    @Size(max = 500, message = "Note must be 500 characters or fewer")
    private String note;

    @NotNull(message = "Position is required")
    private Integer position;

    public ReadingListBookSlotRequestDTO()
    {

    }

    public ReadingListBookSlotRequestDTO(String title, String author, String note, Integer position)
    {
        this.title = title;
        this.author = author;
        this.note = note;
        this.position = position;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
