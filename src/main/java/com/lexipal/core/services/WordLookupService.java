package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.dtos.responses.AchievementResponseDTO;
import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;
import com.lexipal.core.dtos.responses.WordLookupCreateResponseDTO;
import com.lexipal.core.dtos.responses.WordLookupReadResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.entities.WordLookup;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.models.CodexBoardWordDefinition;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.WordLookupRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WordLookupService implements WordLookupServiceInterface
{
    private static final Logger logger = LogManager.getLogger(WordLookupService.class);
    private static final int COMMON_CAPTURE_XP = 10;
    private static final int UNCOMMON_CAPTURE_XP = 20;
    private static final int RARE_CAPTURE_XP = 30;
    private static final int MYTHIC_CAPTURE_XP = 50;

    private final WordLookupRepository wordLookupRepository;
    private final CodexEntryRepository codexEntryRepository;
    private final SessionRepository sessionRepository;
    private final WordService wordService;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CodexBoardService codexBoardService;
    private final AchievementService achievementService;

    public WordLookupService(final WordLookupRepository wordLookupRepository,
                             final CodexEntryRepository codexEntryRepository,
                             final SessionRepository sessionRepository,
                             final WordService wordService,
                             final UserServiceInterface userService,
                             final XpService xpService,
                             final CodexBoardService codexBoardService,
                             final AchievementService achievementService)
    {
        this.wordLookupRepository = wordLookupRepository;
        this.codexEntryRepository = codexEntryRepository;
        this.sessionRepository = sessionRepository;
        this.wordService = wordService;
        this.userService = userService;
        this.xpService = xpService;
        this.codexBoardService = codexBoardService;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public WordLookupCreateResponseDTO lookupWord(Long sessionId, WordLookupCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.warn("Session {} was not found while looking up lemma {} for user {}", sessionId, dto.getLemma(), user.getId());
                    return new ResourceNotFoundException("Session", "id", sessionId);
                });

        if (!session.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to look up a word from session {} they do not own", user.getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        String cleanLemma = normalizeLemma(dto.getLemma());
        Word globalWord = wordService.saveWordFromApi(cleanLemma);
        CodexBoardWordDefinition boardWord = codexBoardService.findByLemma(cleanLemma).orElse(null);

        var existingLookup = wordLookupRepository.findByUser_IdAndWord_Id(user.getId(), globalWord.getId());
        boolean newLookup = existingLookup.isEmpty();
        WordLookup wordLookup = existingLookup.orElseGet(() -> {
            WordLookup lookup = new WordLookup();
            lookup.setUser(user);
            lookup.setWord(globalWord);
            return lookup;
        });

        wordLookup.recordLookup(session.getId());
        WordLookup savedLookup = wordLookupRepository.save(wordLookup);

        if (boardWord == null)
        {
            logger.info("Lookup for '{}' succeeded for user {} but the word is not part of the active codex board", cleanLemma, user.getId());
            return new WordLookupCreateResponseDTO(
                    toWordLookupReadResponse(savedLookup),
                    null,
                    true,
                    false,
                    newLookup,
                    false,
                    false,
                    null,
                    null,
                    null,
                    codexBoardService.getOverallProgressForUser(user),
                    null,
                    xpService.buildZeroAwardUpdate(user)
            );
        }

        var existingEntry = codexEntryRepository.findByUser_IdAndWord_Lemma(user.getId(), cleanLemma);

        if (existingEntry.isPresent())
        {
            logger.info("Returned existing codex entry for lemma '{}' for user {}", cleanLemma, user.getId());
            return new WordLookupCreateResponseDTO(
                    toWordLookupReadResponse(savedLookup),
                    toCodexEntryReadResponse(existingEntry.get(), boardWord),
                    true,
                    true,
                    newLookup,
                    false,
                    true,
                    boardWord.pageNumber(),
                    boardWord.slotNumber(),
                    codexBoardService.getPageProgressForUser(user, boardWord.pageNumber()),
                    codexBoardService.getOverallProgressForUser(user),
                    boardWord.rarityTier(),
                    xpService.buildZeroAwardUpdate(user)
            );
        }

        CodexEntry newCodexEntry = new CodexEntry();
        newCodexEntry.setWord(globalWord);
        newCodexEntry.setUser(user);
        newCodexEntry.setSourceSessionId(session.getId());
        newCodexEntry.setXpValue(resolveCaptureXp(boardWord.rarityTier()));

        codexEntryRepository.save(newCodexEntry);
        user.getCodexEntries().add(newCodexEntry);

        int currentCapturedCount = session.getCapturedWordCount() == null ? 0 : session.getCapturedWordCount();
        session.setCapturedWordCount(currentCapturedCount + 1);
        sessionRepository.save(session);

        XpUpdateResponseDTO xpUpdate = xpService.awardXp(user, newCodexEntry.getXpValue());
        List<AchievementResponseDTO> newlyUnlockedAchievements = achievementService.evaluateAchievements(user);

        logger.info("Added new codex entry '{}' from session {} for user {}", cleanLemma, session.getId(), user.getId());

        return new WordLookupCreateResponseDTO(
                toWordLookupReadResponse(savedLookup),
                toCodexEntryReadResponse(newCodexEntry, boardWord),
                true,
                true,
                newLookup,
                true,
                false,
                boardWord.pageNumber(),
                boardWord.slotNumber(),
                codexBoardService.getPageProgressForUser(user, boardWord.pageNumber()),
                codexBoardService.getOverallProgressForUser(user),
                boardWord.rarityTier(),
                xpUpdate,
                newlyUnlockedAchievements
        );
    }

    @Override
    public List<WordLookupReadResponseDTO> getLookupsForCurrentUser(String keycloakSub)
    {
        userService.getRequiredUser(keycloakSub);

        return wordLookupRepository.findByUser_KeycloakSubOrderByLastLookedUpAtDesc(keycloakSub).stream()
                .map(this::toWordLookupReadResponse)
                .collect(Collectors.toList());
    }

    private WordLookupReadResponseDTO toWordLookupReadResponse(WordLookup wordLookup)
    {
        Word word = wordLookup.getWord();

        return new WordLookupReadResponseDTO(
                wordLookup.getId(),
                word != null ? word.getId() : null,
                word != null ? word.getLemma() : null,
                word != null ? word.getSource() : null,
                word != null ? word.getDefinitions() : null,
                word != null ? word.getPhonetics() : null,
                wordLookup.getFirstLookedUpAt(),
                wordLookup.getLastLookedUpAt(),
                wordLookup.getLookupCount()
        );
    }

    private CodexEntryReadResponseDTO toCodexEntryReadResponse(CodexEntry codexEntry, CodexBoardWordDefinition boardWord)
    {
        return new CodexEntryReadResponseDTO(
                codexEntry.getId(),
                codexEntry.getWord().getId(),
                codexEntry.getSourceSessionId(),
                codexEntry.getWord().getLemma(),
                codexEntry.getSpecificDefinition(),
                codexEntry.getXpValue(),
                codexEntry.getCreatedAt(),
                boardWord != null ? boardWord.rarityScore() : codexEntry.getWord().getRarityScore(),
                boardWord != null ? boardWord.rarityTier() : null,
                codexEntry.getWord().getSource(),
                codexEntry.getWord().getDefinitions(),
                codexEntry.getWord().getPhonetics()
        );
    }

    private int resolveCaptureXp(String rarityTier)
    {
        if (rarityTier == null)
        {
            return COMMON_CAPTURE_XP;
        }

        return switch (rarityTier.trim().toLowerCase())
        {
            case "mythic" -> MYTHIC_CAPTURE_XP;
            case "rare" -> RARE_CAPTURE_XP;
            case "uncommon" -> UNCOMMON_CAPTURE_XP;
            default -> COMMON_CAPTURE_XP;
        };
    }

    private String normalizeLemma(String lemma)
    {
        if (lemma == null || lemma.isBlank())
        {
            throw new IllegalArgumentException("Lemma cannot be null or blank.");
        }

        return lemma.trim().toLowerCase();
    }
}
