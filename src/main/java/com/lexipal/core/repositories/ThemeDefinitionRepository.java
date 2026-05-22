package com.lexipal.core.repositories;

import com.lexipal.core.entities.ThemeDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeDefinitionRepository extends JpaRepository<ThemeDefinition, Long>
{
    @EntityGraph(attributePaths = "storeItem")
    Optional<ThemeDefinition> findByCode(String code);

    @EntityGraph(attributePaths = "storeItem")
    Optional<ThemeDefinition> findByStoreItem_Id(Long storeItemId);

    @EntityGraph(attributePaths = "storeItem")
    List<ThemeDefinition> findByActiveTrueOrderBySortOrderAsc();
}
