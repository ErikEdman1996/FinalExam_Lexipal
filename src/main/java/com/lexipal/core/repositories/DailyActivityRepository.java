package com.lexipal.core.repositories;

import com.lexipal.core.entities.DailyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, Long>
{
    List<DailyActivity> findByUser_KeycloakSubOrderByActivityDateDesc(String keycloakSub);
    Optional<DailyActivity> findBySession_IdAndActivityType(Long sessionId, DailyActivity.ActivityType activityType);

    @Query("""
            select coalesce(sum(case when d.xpEarned > 0 then d.xpEarned else 0 end), 0)
            from DailyActivity d
            where d.user.id = :userId
            """)
    int sumPositiveXpEarnedByUserId(@Param("userId") Long userId);
}
