package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.CaptureCreateRequestDTO;
import com.lexipal.core.dtos.responses.CaptureCreateResponseDTO;
import com.lexipal.core.dtos.responses.CaptureReadResponseDTO;
import com.lexipal.core.entities.CapturedWord;
import com.lexipal.core.entities.Session;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.CapturedWordRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CapturedWordService implements CapturedWordServiceInterface
{
    private static final Logger logger = LogManager.getLogger(CapturedWordService.class);

    private final CapturedWordRepository capturedWordRepository;
    private final WordService wordService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public CapturedWordService(final CapturedWordRepository capturedWordRepository,
                               final WordService wordService,
                               final UserRepository userRepository, SessionRepository sessionRepository)
    {
        this.capturedWordRepository = capturedWordRepository;
        this.wordService = wordService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public CaptureCreateResponseDTO performSearchAndCapture(Long sessionId, CaptureCreateRequestDTO dto, String keycloakSub)
    {
        Optional<User> existingUser = userRepository.findByKeycloakSub(keycloakSub);

        if(existingUser.isEmpty())
        {
            logger.warn("User was not found while capturing lemma {}", dto.getLemma());
            throw new ResourceNotFoundException("User", "sub", keycloakSub);
        }

        Optional<Session> existingSession = sessionRepository.findById(sessionId);

        if(existingSession.isEmpty())
        {
            logger.warn("Session {} was not found while capturing lemma {} for user {}", sessionId, dto.getLemma(), existingUser.get().getId());
            throw new ResourceNotFoundException("Session", "id", sessionId);
        }

        if(!existingSession.get().getUser().getKeycloakSub().equals(keycloakSub))
        {
            logger.warn("User {} attempted to capture a word in session {} they do not own", existingUser.get().getId(), sessionId);
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        String cleanLemma = dto.getLemma().toLowerCase().trim();

        Optional<CapturedWord> existingWord = capturedWordRepository.findByUser_IdAndWord_Lemma(existingUser.get().getId(), cleanLemma);

        if(existingWord.isPresent())
        {
            logger.info("Returned existing captured word for lemma '{}' for user {}", cleanLemma, existingUser.get().getId());
            return new CaptureCreateResponseDTO(toCaptureReadResponse(existingWord.get()), false);
        }

        Word globalWord = wordService.saveWordFromApi(cleanLemma);

        CapturedWord newCapture = new CapturedWord();
        newCapture.setWord(globalWord);
        newCapture.setUser(existingUser.get());
        newCapture.setSession(existingSession.get());
        newCapture.setXpValue(10);

        capturedWordRepository.save(newCapture);
        existingUser.get().getCapturedWords().add(newCapture);
        userRepository.save(existingUser.get());
        existingSession.get().getCapturedWords().add(newCapture);
        sessionRepository.save(existingSession.get());
        logger.info("Captured new word '{}' in session {} for user {}", cleanLemma, existingSession.get().getId(), existingUser.get().getId());

        return new CaptureCreateResponseDTO(toCaptureReadResponse(newCapture), true);
    }

    @Override
    public List<CaptureReadResponseDTO> getAllWordsBySub(String keycloakSub)
    {
        User user = userRepository.findByKeycloakSub(keycloakSub)
                .orElseGet(() -> {
                    logger.warn("User was not found while listing captured words");
                    throw new ResourceNotFoundException("User", "sub", keycloakSub);
                });

        List<CapturedWord> words = capturedWordRepository.findAllBySub(user.getKeycloakSub());

        return words.stream()
                .map(this::toCaptureReadResponse)
                .collect(Collectors.toList());
    }

    private CaptureReadResponseDTO toCaptureReadResponse(CapturedWord capturedWord)
    {
        return new CaptureReadResponseDTO(
                capturedWord.getId(),
                capturedWord.getWord().getId(),
                capturedWord.getSession().getId(),
                capturedWord.getWord().getLemma(),
                capturedWord.getSpecificDefinition(),
                capturedWord.getXpValue(),
                capturedWord.getCapturedAt(),
                capturedWord.getWord().getRarityScore(),
                capturedWord.getWord().getSource(),
                capturedWord.getWord().getDefinitions(),
                capturedWord.getWord().getPhonetics()
        );
    }
}
