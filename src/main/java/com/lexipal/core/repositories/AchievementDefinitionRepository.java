package com.lexipal.core.repositories;

import com.lexipal.core.entities.AchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, Long>
{
    Optional<AchievementDefinition> findByCode(String code);

    List<AchievementDefinition> findByActiveTrueOrderBySortOrderAsc();
}
