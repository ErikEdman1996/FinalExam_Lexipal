package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CodexEntryCreateRequestDTO;
import com.lexipal.core.dtos.responses.CodexEntryCreateResponseDTO;
import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.CodexEntryRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CodexEntryService implements CodexEntryServiceInterface
{
    private static final Logger logger = LogManager.getLogger(CodexEntryService.class);

    private final CodexEntryRepository codexEntryRepository;
    private final WordService wordService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public CodexEntryService(final CodexEntryRepository codexEntryRepository,
                             final WordService wordService,
                             final UserRepository userRepository,
                             final SessionRepository sessionRepository)
    {
        this.codexEntryRepository = codexEntryRepository;
        this.wordService = wordService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public CodexEntryCreateResponseDTO searchAndAddToCodex(Long sessionId, CodexEntryCreateRequestDTO dto, String keycloakSub)
    {
        Optional<User> existingUser = userRepository.findByKeycloakSub(keycloakSub);

        if (existingUser.isEmpty())
        {
            logger.warn("User was not found while adding lemma {} to codex", dto.getLemma());
            throw new ResourceNotFoundException("User", "sub", keycloakSub);
        }

        Optional<Session> existingSession = sessionRepository.findById(sessionId);

        if (existingSession.isEmpty())
        {
            logger.warn("Session {} was not found while adding lemma {} to codex for user {}", sessionId, dto.getLemma(), existingUser.get().getId());
            throw new ResourceNotFoundException("Session", "id", sessionId);
        }

        if (!existingSession.get().getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to add a codex entry from session {} they do not own", existingUser.get().getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        String cleanLemma = dto.getLemma().toLowerCase().trim();

        Optional<CodexEntry> existingEntry = codexEntryRepository.findByUser_IdAndWord_Lemma(existingUser.get().getId(), cleanLemma);

        if (existingEntry.isPresent())
        {
            logger.info("Returned existing codex entry for lemma '{}' for user {}", cleanLemma, existingUser.get().getId());
            return new CodexEntryCreateResponseDTO(toCodexEntryReadResponse(existingEntry.get()), false);
        }

        Word globalWord = wordService.saveWordFromApi(cleanLemma);

        CodexEntry newCodexEntry = new CodexEntry();
        newCodexEntry.setWord(globalWord);
        newCodexEntry.setUser(existingUser.get());
        newCodexEntry.setSourceSessionId(existingSession.get().getId());
        newCodexEntry.setXpValue(10);

        codexEntryRepository.save(newCodexEntry);
        existingUser.get().getCodexEntries().add(newCodexEntry);
        userRepository.save(existingUser.get());

        Session session = existingSession.get();
        session.setCapturedWordCount(session.getCapturedWordCount() + 1);
        sessionRepository.save(session);

        logger.info("Added new codex entry '{}' from session {} for user {}", cleanLemma, session.getId(), existingUser.get().getId());

        return new CodexEntryCreateResponseDTO(toCodexEntryReadResponse(newCodexEntry), true);
    }

    @Override
    public List<CodexEntryReadResponseDTO> getAllCodexEntriesBySub(String keycloakSub)
    {
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    logger.warn("User was not found while listing codex entries");
                    throw new ResourceNotFoundException("User", "sub", keycloakSub);
                });

        List<CodexEntry> words = codexEntryRepository.findAllBySub(user.getKeycloakSub());

        return words.stream()
                .map(this::toCodexEntryReadResponse)
                .collect(Collectors.toList());
    }

    private CodexEntryReadResponseDTO toCodexEntryReadResponse(CodexEntry codexEntry)
    {
        return new CodexEntryReadResponseDTO(
                codexEntry.getId(),
                codexEntry.getWord().getId(),
                codexEntry.getSourceSessionId(),
                codexEntry.getWord().getLemma(),
                codexEntry.getSpecificDefinition(),
                codexEntry.getXpValue(),
                codexEntry.getCreatedAt(),
                codexEntry.getWord().getRarityScore(),
                codexEntry.getWord().getSource(),
                codexEntry.getWord().getDefinitions(),
                codexEntry.getWord().getPhonetics()
        );
    }
}
