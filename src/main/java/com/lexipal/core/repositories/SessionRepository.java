package com.lexipal.core.repositories;

import com.lexipal.core.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>
{
    @Query("SELECT MAX(s.endPage) FROM Session s WHERE s.book.id = :bookId AND s.status = :status")
    Integer findMaxEndPageByBookIdAndStatus(@Param("bookId") Long bookId,
                                            @Param("status") Session.SessionStatus status);

    List<Session> findByBookIdAndStatusOrderByCompletedAtDescSessionDateDesc(Long bookId, Session.SessionStatus status);

    Optional<Session> findFirstByBookIdAndUser_IdAndStatusOrderBySessionDateDesc(Long bookId,
                                                                                  Long userId,
                                                                                  Session.SessionStatus status);

    List<Session> findByUser_IdAndStatus(Long userId, Session.SessionStatus status);

    long countByBookIdAndStatus(Long bookId, Session.SessionStatus status);
}
