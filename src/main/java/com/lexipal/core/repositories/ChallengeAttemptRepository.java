package com.lexipal.core.repositories;

import com.lexipal.core.entities.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Long>
{
    List<ChallengeAttempt> findByUser_KeycloakSubOrderByAttemptedAtDesc(String keycloakSub);

    List<ChallengeAttempt> findByUser_KeycloakSubAndChallenge_IdOrderByAttemptedAtDesc(String keycloakSub, Long challengeId);
}
