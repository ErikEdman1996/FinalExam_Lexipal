package com.lexipal.core.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "note_tag_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_note_tag_assignments", columnNames = {"note_id", "tag_id"})
}, indexes = {
        @Index(name = "idx_note_tag_assignments_note", columnList = "note_id"),
        @Index(name = "idx_note_tag_assignments_tag", columnList = "tag_id")
})
public class NoteTagAssignment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    @JsonBackReference("note-tag-assignments")
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    @JsonBackReference("tag-assignments")
    private NoteTag tag;

    @Column(name = "assigned_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedAt;

    public NoteTagAssignment()
    {

    }

    public NoteTagAssignment(Note note, NoteTag tag)
    {
        this.note = note;
        this.tag = tag;
    }

    @PrePersist
    protected void onCreate()
    {
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public NoteTag getTag() { return tag; }
    public void setTag(NoteTag tag) { this.tag = tag; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
