package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "reading_list_genres", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reading_list_genres", columnNames = {"list_id", "genre_normalized"})
}, indexes = {
        @Index(name = "idx_reading_list_genres_list", columnList = "list_id"),
        @Index(name = "idx_reading_list_genres_normalized", columnList = "genre_normalized")
})
public class ReadingListGenre
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    @JsonBackReference("list-genres")
    private ReadingList list;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(name = "genre_normalized", nullable = false, length = 50)
    private String genreNormalized;

    public ReadingListGenre()
    {

    }

    public ReadingListGenre(ReadingList list, String genre)
    {
        this.list = list;
        setGenre(genre);
    }

    @PrePersist
    @PreUpdate
    protected void normalizeGenre()
    {
        this.genreNormalized = CatalogBook.normalize(this.genre);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReadingList getList() { return list; }
    public void setList(ReadingList list) { this.list = list; }

    public String getGenre() { return genre; }
    public void setGenre(String genre)
    {
        this.genre = genre;
        this.genreNormalized = CatalogBook.normalize(genre);
    }

    public String getGenreNormalized() { return genreNormalized; }
    public void setGenreNormalized(String genreNormalized) { this.genreNormalized = genreNormalized; }
}
