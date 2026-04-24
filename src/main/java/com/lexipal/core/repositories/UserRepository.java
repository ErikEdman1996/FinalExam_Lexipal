package com.lexipal.core.repositories;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.projections.AdminDailyUserProjection;
import com.lexipal.core.repositories.projections.AdminTopUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByKeycloakSub(String sub);
    List<User> findByTotalXpIsNull();
    Optional<User> findByNicknameNormalized(String nicknameNormalized);
    boolean existsByNicknameNormalized(String nicknameNormalized);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("SELECT MIN(u.createdAt) FROM User u WHERE u.createdAt IS NOT NULL")
    LocalDateTime findEarliestCreatedAt();

    @Query(value = """
            SELECT cast(u.created_at as date) as "date",
                   count(*) as "newUsers"
            FROM users u
            WHERE u.created_at >= :start
              AND u.created_at < :end
            GROUP BY cast(u.created_at as date)
            ORDER BY cast(u.created_at as date)
            """, nativeQuery = true)
    List<AdminDailyUserProjection> findDailyNewUsers(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT u.id as "userId",
                   u.nickname as "nickname",
                   coalesce((select sum(s.pages_read)
                             from sessions s
                             where s.user_id = u.id and s.status = 'COMPLETED'), 0) as "totalPagesRead",
                   coalesce((select count(*)
                             from books b
                             where b.user_id = u.id and b.status = 'COMPLETE'), 0) as "completedBooks",
                   coalesce((select count(*)
                             from sessions s2
                             where s2.user_id = u.id and s2.status = 'COMPLETED'), 0) as "completedSessions",
                   coalesce(u.total_xp, 0) as "totalXpEarned"
            FROM users u
            ORDER BY coalesce((select sum(s3.pages_read)
                               from sessions s3
                               where s3.user_id = u.id and s3.status = 'COMPLETED'), 0) DESC,
                     coalesce(u.total_xp, 0) DESC,
                     u.id ASC
            LIMIT 10
            """, nativeQuery = true)
    List<AdminTopUserProjection> findTopUsers();
}
