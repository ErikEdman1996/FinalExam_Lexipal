package com.lexipal.core.repositories;

import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.repositories.projections.StreakLeaderboardEntryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakMetaDataRepository extends JpaRepository<StreakMetaData, Long>
{
    Optional<StreakMetaData> findByUser_KeycloakSub(String keycloakSub);
    Optional<StreakMetaData> findByUser_Id(Long userId);

    @Query(value = """
            SELECT u.id as "userId",
                   u.nickname as "nickname",
                   CASE
                       WHEN sm.last_activity_date = :asOfDate
                            OR sm.last_activity_date = :previousDate
                            OR sm.freeze_protected_through_date = :asOfDate
                            OR sm.freeze_protected_through_date = :previousDate
                       THEN coalesce(sm.current_streak, 0)
                       ELSE 0
                   END as "currentStreak",
                   sm.last_activity_date as "lastActivityDate"
            FROM users u
            LEFT JOIN streak_metadata sm ON sm.user_id = u.id
            WHERE u.status = 'ACTIVE'
              AND u.nickname IS NOT NULL
              AND trim(u.nickname) <> ''
            ORDER BY CASE
                         WHEN sm.last_activity_date = :asOfDate
                              OR sm.last_activity_date = :previousDate
                              OR sm.freeze_protected_through_date = :asOfDate
                              OR sm.freeze_protected_through_date = :previousDate
                         THEN coalesce(sm.current_streak, 0)
                         ELSE 0
                     END DESC,
                     CASE WHEN sm.last_activity_date IS NULL THEN 1 ELSE 0 END ASC,
                     sm.last_activity_date DESC,
                     u.id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<StreakLeaderboardEntryProjection> findStreakLeaderboard(@Param("asOfDate") LocalDate asOfDate,
                                                                 @Param("previousDate") LocalDate previousDate,
                                                                 @Param("limit") int limit);
}
