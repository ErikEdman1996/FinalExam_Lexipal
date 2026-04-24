package com.lexipal.core.repositories;

import com.lexipal.core.entities.DailyActivity;
import com.lexipal.core.repositories.projections.AdminDailyXpProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long>
{
    List<DailyActivity> findByUser_KeycloakSubOrderByActivityDateDesc(String keycloakSub);
    Optional<DailyActivity> findBySession_IdAndActivityType(Long sessionId, DailyActivity.ActivityType activityType);

    boolean existsByUser_IdAndActivityTypeAndExternalRefId(Long userId,
                                                           DailyActivity.ActivityType activityType,
                                                           Long externalRefId);

    @Query("""
            select coalesce(sum(case when d.xpEarned > 0 then d.xpEarned else 0 end), 0)
            from DailyActivity d
            where d.user.id = :userId
            """)
    int sumPositiveXpEarnedByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN d.xpEarned > 0 THEN d.xpEarned ELSE 0 END), 0)
            FROM DailyActivity d
            WHERE d.activityDate >= :start
              AND d.activityDate <= :end
            """)
    long sumPositiveXpEarnedBetween(@Param("start") LocalDate start,
                                    @Param("end") LocalDate end);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN d.xpEarned > 0 THEN d.xpEarned ELSE 0 END), 0)
            FROM DailyActivity d
            """)
    long sumAllPositiveXpEarned();

    @Query(value = """
            SELECT d.activity_date as "date",
                   coalesce(sum(case when d.xp_earned > 0 then d.xp_earned else 0 end), 0) as "xpAwarded"
            FROM daily_activities d
            WHERE d.activity_date >= :start
              AND d.activity_date <= :end
            GROUP BY d.activity_date
            ORDER BY d.activity_date
            """, nativeQuery = true)
    List<AdminDailyXpProjection> findDailyXpAwarded(@Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);
}
