package com.lexipal.core.repositories;

import com.lexipal.core.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>
{
    List<Note> findAllByBook_IdAndUser_IdOrderByPageNumberAscCreatedAtAsc(Long bookId, Long userId);

    @Query("SELECT n FROM Note n WHERE n.user.keycloakSub = :sub ORDER BY n.createdAt DESC")
    List<Note> findAllBySub(@Param("sub") String sub);

    List<Note> findByBook_IdOrderByCreatedAtDesc(Long bookId);
}
