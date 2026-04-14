package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.ChallengeReadResponseDTO;

import java.util.List;

public interface ChallengeServiceInterface
{
    List<ChallengeReadResponseDTO> getAllChallenges();

    ChallengeReadResponseDTO getChallengeById(Long challengeId);
}
