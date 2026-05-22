package com.lexipal.core.repositories;

import com.lexipal.core.entities.StoreItemDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemDefinitionRepository extends JpaRepository<StoreItemDefinition, Long>
{
    Optional<StoreItemDefinition> findByCode(String code);

    List<StoreItemDefinition> findByActiveTrueOrderBySortOrderAsc();
}
