package com.lexipal.core.repositories;

import com.lexipal.core.entities.CrosswordPuzzle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrosswordPuzzleRepository extends JpaRepository<CrosswordPuzzle, Long>
{
    @EntityGraph(attributePaths = {"user", "challenge"})
    Optional<CrosswordPuzzle> findByIdAndUser_KeycloakSub(Long id, String keycloakSub);
}
