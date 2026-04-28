package com.lexipal.core.repositories;

import com.lexipal.core.entities.WordLookup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordLookupRepository extends JpaRepository<WordLookup, Long>
{
    @EntityGraph(attributePaths = "word")
    Optional<WordLookup> findByUser_IdAndWord_Id(Long userId, Long wordId);

    @EntityGraph(attributePaths = "word")
    List<WordLookup> findByUser_KeycloakSubOrderByLastLookedUpAtDesc(String keycloakSub);
}
