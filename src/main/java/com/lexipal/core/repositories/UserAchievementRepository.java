package com.lexipal.core.repositories;

import com.lexipal.core.entities.UserAchievement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long>
{
    @EntityGraph(attributePaths = "achievement")
    List<UserAchievement> findByUser_Id(Long userId);

    boolean existsByUser_IdAndAchievement_Id(Long userId, Long achievementId);
}
