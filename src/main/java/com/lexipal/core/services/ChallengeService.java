package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.ChallengeReadResponseDTO;
import com.lexipal.core.entities.Challenge;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService implements ChallengeServiceInterface
{
    private final ChallengeRepository challengeRepository;

    @Autowired
    public ChallengeService(final ChallengeRepository challengeRepository)
    {
        this.challengeRepository = challengeRepository;
    }

    @Override
    public List<ChallengeReadResponseDTO> getAllChallenges()
    {
        return challengeRepository.findAll().stream()
                .map(this::toChallengeReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ChallengeReadResponseDTO getChallengeById(Long challengeId)
    {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        return toChallengeReadResponse(challenge);
    }

    private ChallengeReadResponseDTO toChallengeReadResponse(Challenge challenge)
    {
        return new ChallengeReadResponseDTO(
                challenge.getId(),
                challenge.getType().name(),
                challenge.getDescription(),
                challenge.getDifficulty().name()
        );
    }
}
