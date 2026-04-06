package com.lexipal.core.services;

import com.lexipal.core.dtos.CaptureRequestDTO;
import com.lexipal.core.dtos.CaptureResponseDTO;
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

import java.util.Optional;

@Service
public class CapturedWordService implements CapturedWordServiceInterface
{
    private final CapturedWordRepository capturedWordRepository;
    private final WordService wordService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    private final static Logger capturedWordLogger = LogManager.getLogger("CapturedWordLogger");
    private final static Logger userLogger = LogManager.getLogger("UserLogger");
    private final static Logger sessionLogger = LogManager.getLogger("SessionLogger");

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
    public CaptureResponseDTO performSearchAndCapture(Long sessionId, CaptureRequestDTO dto, String keycloakSub)
    {
        userLogger.info("Retrieving user...");
        Optional<User> existingUser = userRepository.findByKeycloakSub(keycloakSub);

        if(existingUser.isEmpty())
        {
            userLogger.error("User not found");
            throw new ResourceNotFoundException("User", "sub", keycloakSub);
        }

        userLogger.info("User found with id: {}", existingUser.get().getId());

        sessionLogger.info("Retrieving session with id: {}", sessionId);
        Optional<Session> existingSession = sessionRepository.findById(sessionId);

        if(existingSession.isEmpty())
        {
            sessionLogger.error("Session with id not found: {}", sessionId);
            throw new ResourceNotFoundException("Session", "id", sessionId);
        }

        sessionLogger.info("Session found with id: {}", existingSession.get().getId());

        if(!existingSession.get().getUser().getKeycloakSub().equals(keycloakSub))
        {
            userLogger.error("This session does not belong to you.");
            throw new UnauthorizedActionException("This session does not belong to you.");
        }

        String cleanLemma = dto.getLemma().toLowerCase().trim();

        capturedWordLogger.info("Searching for captured word with lemma: {} for user with id: {}", cleanLemma, existingUser.get().getId());
        Optional<CapturedWord> existingWord = capturedWordRepository.findByUser_IdAndWord_Lemma(existingUser.get().getId(), cleanLemma);

        if(existingWord.isPresent())
        {
            capturedWordLogger.info("Word found with lemma: {} found, sending it back", cleanLemma);
            return new CaptureResponseDTO(existingWord.get(), false);
        }

        capturedWordLogger.info("Word not found with lemma: {}, creating new word", cleanLemma);
        Word globalWord = wordService.saveWordFromApi(cleanLemma);

        CapturedWord newCapture = new CapturedWord();
        newCapture.setWord(globalWord);
        newCapture.setUser(existingUser.get());
        newCapture.setSession(existingSession.get());
        newCapture.setXpValue(10);

        capturedWordLogger.info("Created new word with lemma: {}", cleanLemma);
        capturedWordRepository.save(newCapture);

        userLogger.info("Adding word {} to user with id: {}", cleanLemma, existingUser.get().getId());
        existingUser.get().getCapturedWords().add(newCapture);
        userRepository.save(existingUser.get());

        sessionLogger.info("Adding word {} to session with id: {}", cleanLemma, existingSession.get().getId());
        existingSession.get().getCapturedWords().add(newCapture);
        sessionRepository.save(existingSession.get());

        return new CaptureResponseDTO(newCapture, true);
    }
}
