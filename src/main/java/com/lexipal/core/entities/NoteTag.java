package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "note_tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_note_tags_user_name", columnNames = {"user_id", "name_normalized"})
}, indexes = {
        @Index(name = "idx_note_tags_user", columnList = "user_id"),
        @Index(name = "idx_note_tags_user_normalized", columnList = "user_id, name_normalized")
})
public class NoteTag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-note-tags")
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "name_normalized", nullable = false, length = 50)
    private String nameNormalized;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tag-assignments")
    private List<NoteTagAssignment> assignments = new ArrayList<>();

    public NoteTag()
    {

    }

    @PrePersist
    protected void onCreate()
    {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.nameNormalized = CatalogBook.normalize(this.name);
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
        this.nameNormalized = CatalogBook.normalize(this.name);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name)
    {
        this.name = name;
        this.nameNormalized = CatalogBook.normalize(name);
    }

    public String getNameNormalized() { return nameNormalized; }
    public void setNameNormalized(String nameNormalized) { this.nameNormalized = nameNormalized; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<NoteTagAssignment> getAssignments() { return assignments; }
    public void setAssignments(List<NoteTagAssignment> assignments) { this.assignments = assignments; }
}
