package com.lexipal.core.dtos;

public class BookSummaryDTO
{
    private long id;
    private String title;
    private String author;
    private Integer readPages;
    private Integer totalPages;
    private double progressPercentage;
    private String status;
    private int sessionCount;

    public BookSummaryDTO()
    {

    }

    public BookSummaryDTO(long id, String title, String author, Integer readPages, Integer totalPages, double progressPercentage, String status, int sessionCount)
    {
        this.id = id;
        this.title = title;
        this.author = author;
        this.readPages = readPages;
        this.totalPages = totalPages;
        this.progressPercentage = progressPercentage;
        this.status = status;
        this.sessionCount = sessionCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getReadPages() {
        return readPages;
    }

    public void setReadPages(Integer readPages) {
        this.readPages = readPages;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
}
