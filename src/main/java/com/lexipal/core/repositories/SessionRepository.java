package com.lexipal.core.repositories;

import com.lexipal.core.entities.Session;
import com.lexipal.core.repositories.projections.AdminDailySessionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    List<Session> findByBook_IdOrderBySessionDateDesc(Long bookId);

    @Query("""
            SELECT COALESCE(SUM(s.pagesRead), 0) FROM Session s
            WHERE s.user.id = :userId
              AND s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
            """)
    int sumCompletedPagesByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(s.completedAt) FROM Session s WHERE s.book.id = :bookId AND s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED")
    LocalDateTime findLatestCompletedAtByBookId(@Param("bookId") Long bookId);

    @Query("""
            SELECT MIN(s.completedAt) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
              AND s.completedAt IS NOT NULL
            """)
    LocalDateTime findEarliestCompletedAt();

    @Query("""
            SELECT COALESCE(SUM(s.pagesRead), 0) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
              AND s.completedAt >= :start
              AND s.completedAt < :end
            """)
    long sumPagesReadCompletedBetween(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(s.durationSeconds), 0) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
              AND s.completedAt >= :start
              AND s.completedAt < :end
            """)
    long sumDurationSecondsCompletedBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(s) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
              AND s.completedAt >= :start
              AND s.completedAt < :end
            """)
    long countCompletedBetween(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("""
            SELECT COUNT(DISTINCT s.user.id) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
              AND s.completedAt >= :start
              AND s.completedAt < :end
            """)
    long countActiveUsersBetween(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(s.pagesRead), 0) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
            """)
    long sumAllCompletedPagesRead();

    @Query("""
            SELECT COUNT(s) FROM Session s
            WHERE s.status = com.lexipal.core.entities.Session.SessionStatus.COMPLETED
            """)
    long countAllCompleted();

    @Query(value = """
            SELECT cast(s.completed_at as date) as "date",
                   coalesce(sum(s.pages_read), 0) as "pagesRead",
                   count(*) as "completedSessions",
                   count(distinct s.user_id) as "activeUsers"
            FROM sessions s
            WHERE s.status = 'COMPLETED'
              AND s.completed_at >= :start
              AND s.completed_at < :end
            GROUP BY cast(s.completed_at as date)
            ORDER BY cast(s.completed_at as date)
            """, nativeQuery = true)
    List<AdminDailySessionProjection> findDailySessionStats(@Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end);
}
