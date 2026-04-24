package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CodexEntryCreateRequestDTO;
import com.lexipal.core.dtos.responses.CodexEntryCreateResponseDTO;
import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;
import com.lexipal.core.models.CodexBoardWordDefinition;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.SessionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CodexEntryService implements CodexEntryServiceInterface
{
    private static final Logger logger = LogManager.getLogger(CodexEntryService.class);
    private static final int COMMON_CAPTURE_XP = 10;
    private static final int UNCOMMON_CAPTURE_XP = 20;
    private static final int RARE_CAPTURE_XP = 30;
    private static final int MYTHIC_CAPTURE_XP = 50;

    private final CodexEntryRepository codexEntryRepository;
    private final WordService wordService;
    private final SessionRepository sessionRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CodexBoardService codexBoardService;
    private final AchievementService achievementService;

    @Autowired
    public CodexEntryService(final CodexEntryRepository codexEntryRepository,
                             final WordService wordService,
                             final SessionRepository sessionRepository,
                             final UserServiceInterface userService,
                             final XpService xpService,
                             final CodexBoardService codexBoardService,
                             final AchievementService achievementService)
    {
        this.codexEntryRepository = codexEntryRepository;
        this.wordService = wordService;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
        this.xpService = xpService;
        this.codexBoardService = codexBoardService;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public CodexEntryCreateResponseDTO searchAndAddToCodex(Long sessionId, CodexEntryCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.warn("Session {} was not found while adding lemma {} to codex for user {}", sessionId, dto.getLemma(), user.getId());
                    return new ResourceNotFoundException("Session", "id", sessionId);
                });

        if (!session.getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to add a codex entry from session {} they do not own", user.getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        String cleanLemma = dto.getLemma().toLowerCase().trim();
        Word globalWord = wordService.saveWordFromApi(cleanLemma);
        CodexBoardWordDefinition boardWord = codexBoardService.findByLemma(cleanLemma).orElse(null);

        if (boardWord == null)
        {
            logger.info("Lookup for '{}' succeeded for user {} but the word is not part of the active codex board", cleanLemma, user.getId());
            return new CodexEntryCreateResponseDTO(
                    toWordLookupResponse(globalWord, null),
                    true,
                    false,
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
            return new CodexEntryCreateResponseDTO(
                    toCodexEntryReadResponse(existingEntry.get(), boardWord),
                    true,
                    true,
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

        session.setCapturedWordCount(session.getCapturedWordCount() + 1);
        sessionRepository.save(session);
        achievementService.evaluateAchievements(user);

        logger.info("Added new codex entry '{}' from session {} for user {}", cleanLemma, session.getId(), user.getId());

        return new CodexEntryCreateResponseDTO(
                toCodexEntryReadResponse(newCodexEntry, boardWord),
                true,
                true,
                true,
                false,
                boardWord.pageNumber(),
                boardWord.slotNumber(),
                codexBoardService.getPageProgressForUser(user, boardWord.pageNumber()),
                codexBoardService.getOverallProgressForUser(user),
                boardWord.rarityTier(),
                xpService.awardXp(user, newCodexEntry.getXpValue())
        );
    }

    @Override
    public List<CodexEntryReadResponseDTO> getAllCodexEntriesBySub(String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        List<CodexEntry> words = codexEntryRepository.findAllBySub(user.getKeycloakSub());

        return words.stream()
                .map(codexEntry -> toCodexEntryReadResponse(
                        codexEntry,
                        codexBoardService.findByLemma(codexEntry.getWord().getLemma()).orElse(null)
                ))
                .collect(Collectors.toList());
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

    private CodexEntryReadResponseDTO toWordLookupResponse(Word word, CodexBoardWordDefinition boardWord)
    {
        return new CodexEntryReadResponseDTO(
                null,
                word.getId(),
                null,
                word.getLemma(),
                null,
                null,
                null,
                boardWord != null ? boardWord.rarityScore() : word.getRarityScore(),
                boardWord != null ? boardWord.rarityTier() : null,
                word.getSource(),
                word.getDefinitions(),
                word.getPhonetics()
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
}
