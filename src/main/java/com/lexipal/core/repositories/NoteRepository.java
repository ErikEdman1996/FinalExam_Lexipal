package com.lexipal.core.repositories;

import com.lexipal.core.entities.Note;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>
{
    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    List<Note> findAllByBook_IdAndUser_IdOrderByPageNumberAscCreatedAtAsc(Long bookId, Long userId);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    @Query("SELECT n FROM Note n WHERE n.user.keycloakSub = :sub ORDER BY n.createdAt DESC")
    List<Note> findAllBySub(@Param("sub") String sub);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    List<Note> findByBook_IdOrderByCreatedAtDesc(Long bookId);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    @Query("""
            SELECT DISTINCT n FROM Note n
            JOIN n.tagAssignments a
            WHERE n.user.keycloakSub = :sub
              AND a.tag.id IN :tagIds
            ORDER BY n.createdAt DESC
            """)
    List<Note> findAllBySubAndAnyTag(@Param("sub") String sub, @Param("tagIds") List<Long> tagIds);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    @Query("""
            SELECT n FROM Note n
            WHERE n.user.keycloakSub = :sub
              AND (
                    SELECT COUNT(DISTINCT a.tag.id)
                    FROM NoteTagAssignment a
                    WHERE a.note = n
                      AND a.tag.id IN :tagIds
                  ) = :tagCount
            ORDER BY n.createdAt DESC
            """)
    List<Note> findAllBySubAndAllTags(@Param("sub") String sub,
                                      @Param("tagIds") List<Long> tagIds,
                                      @Param("tagCount") long tagCount);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    @Query("""
            SELECT DISTINCT n FROM Note n
            JOIN n.tagAssignments a
            WHERE n.book.id = :bookId
              AND n.user.id = :userId
              AND a.tag.id IN :tagIds
            ORDER BY n.pageNumber ASC, n.createdAt ASC
            """)
    List<Note> findAllByBookAndUserAndAnyTag(@Param("bookId") Long bookId,
                                             @Param("userId") Long userId,
                                             @Param("tagIds") List<Long> tagIds);

    @EntityGraph(attributePaths = {"tagAssignments", "tagAssignments.tag"})
    @Query("""
            SELECT n FROM Note n
            WHERE n.book.id = :bookId
              AND n.user.id = :userId
              AND (
                    SELECT COUNT(DISTINCT a.tag.id)
                    FROM NoteTagAssignment a
                    WHERE a.note = n
                      AND a.tag.id IN :tagIds
                  ) = :tagCount
            ORDER BY n.pageNumber ASC, n.createdAt ASC
            """)
    List<Note> findAllByBookAndUserAndAllTags(@Param("bookId") Long bookId,
                                              @Param("userId") Long userId,
                                              @Param("tagIds") List<Long> tagIds,
                                              @Param("tagCount") long tagCount);
}
