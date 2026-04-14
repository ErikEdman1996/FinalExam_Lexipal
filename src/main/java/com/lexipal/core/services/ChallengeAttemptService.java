package com.lexipal.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.lexipal.core.dtos.requests.ChallengeAttemptCreateRequestDTO;
import com.lexipal.core.dtos.responses.ChallengeAttemptReadResponseDTO;
import com.lexipal.core.entities.*;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.ChallengeAttemptRepository;
import com.lexipal.core.repositories.ChallengeRepository;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.WordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeAttemptService implements ChallengeAttemptServiceInterface
{
    private static final int PASS_XP = 10;
    private static final int FAIL_XP = 0;

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeRepository challengeRepository;
    private final CodexEntryRepository codexEntryRepository;
    private final WordRepository wordRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;

    @Autowired
    public ChallengeAttemptService(final ChallengeAttemptRepository challengeAttemptRepository,
                                   final ChallengeRepository challengeRepository,
                                   final CodexEntryRepository codexEntryRepository,
                                   final WordRepository wordRepository,
                                   final UserServiceInterface userService,
                                   final XpService xpService)
    {
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.challengeRepository = challengeRepository;
        this.codexEntryRepository = codexEntryRepository;
        this.wordRepository = wordRepository;
        this.userService = userService;
        this.xpService = xpService;
    }

    @Override
    @Transactional
    public ChallengeAttemptReadResponseDTO createAttempt(Long challengeId, ChallengeAttemptCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        validateReferenceInputs(dto);

        CodexEntry codexEntry = null;
        Word word = null;

        if (dto.getCodexEntryId() != null)
        {
            codexEntry = codexEntryRepository.findById(dto.getCodexEntryId())
                    .orElseThrow(() -> new ResourceNotFoundException("CodexEntry", "id", dto.getCodexEntryId()));

            if (!codexEntry.getUser().getKeycloakSub().equals(keycloakSub))
            {
                throw new UnauthorizedActionException("This codex entry does not belong to you.");
            }
        }

        if (dto.getWordId() != null)
        {
            word = wordRepository.findById(dto.getWordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Word", "id", dto.getWordId()));
        }

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setUser(user);
        attempt.setChallenge(challenge);
        attempt.setUserAnswer(dto.getUserAnswer());
        attempt.setResult(dto.getResult());
        attempt.setXpAwarded(calculateXpAwarded(dto.getResult()));
        attempt.setCodexEntry(codexEntry);
        attempt.setWord(word);

        ChallengeAttempt savedAttempt = challengeAttemptRepository.save(attempt);
        user.getChallengeAttempts().add(savedAttempt);
        challenge.getAttempts().add(savedAttempt);

        return toChallengeAttemptReadResponse(savedAttempt, xpService.awardXp(user, savedAttempt.getXpAwarded()));
    }

    @Override
    public List<ChallengeAttemptReadResponseDTO> getAttemptsForCurrentUser(String keycloakSub)
    {
        userService.getRequiredUser(keycloakSub);

        return challengeAttemptRepository.findByUser_KeycloakSubOrderByAttemptedAtDesc(keycloakSub).stream()
                .map(this::toChallengeAttemptReadResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChallengeAttemptReadResponseDTO> getAttemptsForCurrentUserByChallenge(Long challengeId, String keycloakSub)
    {
        userService.getRequiredUser(keycloakSub);

        challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        return challengeAttemptRepository.findByUser_KeycloakSubAndChallenge_IdOrderByAttemptedAtDesc(keycloakSub, challengeId).stream()
                .map(this::toChallengeAttemptReadResponse)
                .collect(Collectors.toList());
    }

    private void validateReferenceInputs(ChallengeAttemptCreateRequestDTO dto)
    {
        boolean hasCodexEntry = dto.getCodexEntryId() != null;
        boolean hasWord = dto.getWordId() != null;

        if (hasCodexEntry == hasWord)
        {
            throw new IllegalArgumentException("Exactly one of codexEntryId or wordId must be provided.");
        }
    }

    private int calculateXpAwarded(ChallengeResult result)
    {
        return result == ChallengeResult.PASS ? PASS_XP : FAIL_XP;
    }

    private ChallengeAttemptReadResponseDTO toChallengeAttemptReadResponse(ChallengeAttempt attempt)
    {
        return toChallengeAttemptReadResponse(attempt, null);
    }

    private ChallengeAttemptReadResponseDTO toChallengeAttemptReadResponse(ChallengeAttempt attempt,
                                                                           com.lexipal.core.dtos.responses.XpUpdateResponseDTO xpUpdate)
    {
        CodexEntry codexEntry = attempt.getCodexEntry();
        Word sourceWord = codexEntry != null ? codexEntry.getWord() : attempt.getWord();
        String definitionPreview = codexEntry != null && codexEntry.getSpecificDefinition() != null
                ? codexEntry.getSpecificDefinition()
                : extractDefinitionPreview(sourceWord);

        return new ChallengeAttemptReadResponseDTO(
                attempt.getId(),
                attempt.getUserAnswer(),
                attempt.getResult().name(),
                attempt.getXpAwarded(),
                attempt.getAttemptedAt(),
                attempt.getChallenge().getId(),
                attempt.getChallenge().getType().name(),
                codexEntry != null ? codexEntry.getId() : null,
                attempt.getWord() != null ? attempt.getWord().getId() : null,
                sourceWord != null ? sourceWord.getLemma() : null,
                definitionPreview,
                xpUpdate
        );
    }

    private String extractDefinitionPreview(Word word)
    {
        if (word == null || word.getDefinitions() == null)
        {
            return null;
        }

        JsonNode definitions = word.getDefinitions();

        if (definitions.isArray() && !definitions.isEmpty())
        {
            JsonNode first = definitions.get(0);
            if (first != null)
            {
                JsonNode sensesNode = first.get("senses");
                if (sensesNode != null && sensesNode.isArray() && !sensesNode.isEmpty())
                {
                    JsonNode firstSense = sensesNode.get(0);
                    if (firstSense != null)
                    {
                        JsonNode definitionNode = firstSense.get("definition");
                        if (definitionNode != null && definitionNode.isTextual())
                        {
                            return definitionNode.asText();
                        }
                    }
                }

                if (first.isTextual())
                {
                    return first.asText();
                }

                JsonNode definitionNode = first.get("definition");
                if (definitionNode != null && definitionNode.isTextual())
                {
                    return definitionNode.asText();
                }
            }
        }

        if (definitions.isTextual())
        {
            return definitions.asText();
        }

        return definitions.toString();
    }
}
