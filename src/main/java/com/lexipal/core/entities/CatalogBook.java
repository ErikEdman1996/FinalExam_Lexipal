package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "catalog_books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_catalog_books", columnNames = {"title_normalized", "author_normalized"})
})
public class CatalogBook
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 200)
    private String author;

    @Column(name = "title_normalized", nullable = false, length = 200)
    private String titleNormalized;

    @Column(name = "author_normalized", nullable = false, length = 200)
    private String authorNormalized;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public CatalogBook()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        if (this.titleNormalized == null)
        {
            this.titleNormalized = normalize(this.title);
        }
        if (this.authorNormalized == null)
        {
            this.authorNormalized = normalize(this.author);
        }
    }

    public static String normalize(String value)
    {
        if (value == null)
        {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.titleNormalized = normalize(title);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        this.authorNormalized = normalize(author);
    }

    public String getTitleNormalized() {
        return titleNormalized;
    }

    public void setTitleNormalized(String titleNormalized) {
        this.titleNormalized = titleNormalized;
    }

    public String getAuthorNormalized() {
        return authorNormalized;
    }

    public void setAuthorNormalized(String authorNormalized) {
        this.authorNormalized = authorNormalized;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
