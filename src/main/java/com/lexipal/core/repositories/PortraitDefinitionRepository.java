package com.lexipal.core.repositories;

import com.lexipal.core.entities.PortraitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortraitDefinitionRepository extends JpaRepository<PortraitDefinition, Long>
{
    Optional<PortraitDefinition> findByCode(String code);

    List<PortraitDefinition> findByActiveTrueOrderBySortOrderAsc();
}
