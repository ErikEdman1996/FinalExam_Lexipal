package com.lexipal.core.repositories;

import com.lexipal.core.entities.StreakMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StreakMetaDataRepository extends JpaRepository<StreakMetaData, Long>
{
    Optional<StreakMetaData> findByUser_KeycloakSub(String keycloakSub);
    Optional<StreakMetaData> findByUser_Id(Long userId);
}
