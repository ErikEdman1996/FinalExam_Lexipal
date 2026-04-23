package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "reading_list_books", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reading_list_books_position", columnNames = {"list_id", "position"}),
        @UniqueConstraint(name = "uk_reading_list_books_catalog", columnNames = {"list_id", "catalog_book_id"})
})
public class ReadingListBook
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    @JsonBackReference("list-books")
    private ReadingList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_book_id", nullable = false)
    private CatalogBook catalogBook;

    @Column(nullable = false)
    private Integer position;

    @Column(length = 500)
    private String note;

    public ReadingListBook()
    {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReadingList getList() {
        return list;
    }

    public void setList(ReadingList list) {
        this.list = list;
    }

    public CatalogBook getCatalogBook() {
        return catalogBook;
    }

    public void setCatalogBook(CatalogBook catalogBook) {
        this.catalogBook = catalogBook;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
