package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.WordLookupCreateRequestDTO;
import com.lexipal.core.dtos.responses.WordLookupCreateResponseDTO;
import com.lexipal.core.dtos.responses.WordLookupReadResponseDTO;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.entities.WordLookup;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
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

    private final WordLookupRepository wordLookupRepository;
    private final SessionRepository sessionRepository;
    private final WordService wordService;
    private final UserServiceInterface userService;

    public WordLookupService(final WordLookupRepository wordLookupRepository,
                             final SessionRepository sessionRepository,
                             final WordService wordService,
                             final UserServiceInterface userService)
    {
        this.wordLookupRepository = wordLookupRepository;
        this.sessionRepository = sessionRepository;
        this.wordService = wordService;
        this.userService = userService;
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

        logger.info("Recorded word lookup '{}' from session {} for user {}", cleanLemma, session.getId(), user.getId());

        return new WordLookupCreateResponseDTO(
                toWordLookupReadResponse(savedLookup),
                true,
                newLookup
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

    private String normalizeLemma(String lemma)
    {
        if (lemma == null || lemma.isBlank())
        {
            throw new IllegalArgumentException("Lemma cannot be null or blank.");
        }

        return lemma.trim().toLowerCase();
    }
}
