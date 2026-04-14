package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.ChallengeAttemptCreateRequestDTO;
import com.lexipal.core.dtos.responses.ChallengeAttemptReadResponseDTO;

import java.util.List;

public interface ChallengeAttemptServiceInterface
{
    ChallengeAttemptReadResponseDTO createAttempt(Long challengeId, ChallengeAttemptCreateRequestDTO dto, String keycloakSub);

    List<ChallengeAttemptReadResponseDTO> getAttemptsForCurrentUser(String keycloakSub);

    List<ChallengeAttemptReadResponseDTO> getAttemptsForCurrentUserByChallenge(Long challengeId, String keycloakSub);
}
