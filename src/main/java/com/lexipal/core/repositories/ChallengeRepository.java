package com.lexipal.core.repositories;

import com.lexipal.core.entities.Challenge;
import com.lexipal.core.entities.ChallengeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long>
{
    Optional<Challenge> findByType(ChallengeType type);
}
