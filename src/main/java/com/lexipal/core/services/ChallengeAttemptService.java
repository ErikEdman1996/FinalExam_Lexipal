package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.ChallengeAttemptCreateRequestDTO;
import com.lexipal.core.dtos.responses.ChallengeAttemptReadResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.Challenge;
import com.lexipal.core.entities.ChallengeAttempt;
import com.lexipal.core.entities.ChallengeResult;
import com.lexipal.core.entities.ChallengeType;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.entities.WordLookup;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.ChallengeAttemptRepository;
import com.lexipal.core.repositories.ChallengeRepository;
import com.lexipal.core.repositories.WordLookupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChallengeAttemptService implements ChallengeAttemptServiceInterface
{
    private static final int PASS_XP = 10;
    private static final int FAIL_XP = 0;

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeRepository challengeRepository;
    private final WordLookupRepository wordLookupRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CurrencyService currencyService;

    @Autowired
    public ChallengeAttemptService(final ChallengeAttemptRepository challengeAttemptRepository,
                                   final ChallengeRepository challengeRepository,
                                   final WordLookupRepository wordLookupRepository,
                                   final UserServiceInterface userService,
                                   final XpService xpService,
                                   final CurrencyService currencyService)
    {
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.challengeRepository = challengeRepository;
        this.wordLookupRepository = wordLookupRepository;
        this.userService = userService;
        this.xpService = xpService;
        this.currencyService = currencyService;
    }

    @Override
    @Transactional
    public ChallengeAttemptReadResponseDTO createAttempt(Long challengeId, ChallengeAttemptCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "id", challengeId));

        if (challenge.getType() == ChallengeType.CROSSWORD)
        {
            throw new IllegalArgumentException("Crossword attempts must be submitted through the crossword puzzle endpoint.");
        }

        WordLookup wordLookup = wordLookupRepository.findById(dto.getWordLookupId())
                .orElseThrow(() -> new ResourceNotFoundException("WordLookup", "id", dto.getWordLookupId()));

        if (!wordLookup.getUser().getKeycloakSub().equals(keycloakSub))
        {
            throw new UnauthorizedActionException("This word lookup does not belong to you.");
        }

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setUser(user);
        attempt.setChallenge(challenge);
        attempt.setUserAnswer(dto.getUserAnswer());
        attempt.setResult(dto.getResult());
        attempt.setXpAwarded(calculateXpAwarded(dto.getResult()));
        attempt.setWordLookup(wordLookup);

        ChallengeAttempt savedAttempt = challengeAttemptRepository.save(attempt);
        user.getChallengeAttempts().add(savedAttempt);
        challenge.getAttempts().add(savedAttempt);

        XpUpdateResponseDTO xpUpdate = xpService.awardXp(user, savedAttempt.getXpAwarded());
        if (savedAttempt.getResult() == ChallengeResult.PASS)
        {
            currencyService.awardCoins(user,
                    CurrencyTransactionReason.CHALLENGE_PASSED,
                    "CHALLENGE_ATTEMPT", savedAttempt.getId());
        }

        return toChallengeAttemptReadResponse(savedAttempt, xpUpdate);
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

    private int calculateXpAwarded(ChallengeResult result)
    {
        return result == ChallengeResult.PASS ? PASS_XP : FAIL_XP;
    }

    private ChallengeAttemptReadResponseDTO toChallengeAttemptReadResponse(ChallengeAttempt attempt)
    {
        return toChallengeAttemptReadResponse(attempt, null);
    }

    private ChallengeAttemptReadResponseDTO toChallengeAttemptReadResponse(ChallengeAttempt attempt,
                                                                           XpUpdateResponseDTO xpUpdate)
    {
        WordLookup wordLookup = attempt.getWordLookup();
        Word sourceWord = wordLookup != null ? wordLookup.getWord() : null;

        return new ChallengeAttemptReadResponseDTO(
                attempt.getId(),
                attempt.getUserAnswer(),
                attempt.getResult().name(),
                attempt.getXpAwarded(),
                attempt.getAttemptedAt(),
                attempt.getChallenge().getId(),
                attempt.getChallenge().getType().name(),
                wordLookup != null ? wordLookup.getId() : null,
                sourceWord != null ? sourceWord.getId() : null,
                sourceWord != null ? sourceWord.getLemma() : null,
                sourceWord != null ? WordDefinitionExtractor.extractPreview(sourceWord.getDefinitions()) : null,
                attempt.getCrosswordPuzzle() != null ? attempt.getCrosswordPuzzle().getId() : null,
                xpUpdate
        );
    }
}
