package com.lexipal.core.services;

import com.lexipal.core.dtos.CaptureRequestDTO;
import com.lexipal.core.dtos.CaptureResponseDTO;
import com.lexipal.core.entities.CapturedWord;
import com.lexipal.core.entities.Word;
import com.lexipal.core.repositories.CapturedWordRepository;
import com.lexipal.core.repositories.SessionRepository;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CapturedWordService implements CapturedWordServiceInterface
{
    private final CapturedWordRepository capturedWordRepository;
    private final WordService wordService;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Autowired
    public CapturedWordService(final CapturedWordRepository capturedWordRepository, final WordService wordService, final UserRepository userRepository, SessionRepository sessionRepository)
    {
        this.capturedWordRepository = capturedWordRepository;
        this.wordService = wordService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public CaptureResponseDTO performSearchAndCapture(CaptureRequestDTO dto)
    {
        String cleanLemma = dto.getLemma().toLowerCase().trim();

        Optional<CapturedWord> existingWord = capturedWordRepository.findByUser_IdAndWord_Lemma(dto.getUserId(), cleanLemma);

        if(existingWord.isPresent())
        {
            return new CaptureResponseDTO(existingWord.get(), false);
        }

        Word globalWord = wordService.saveWordFromApi(cleanLemma);

        CapturedWord newCapture = new CapturedWord();
        newCapture.setWord(globalWord);
        newCapture.setUser(userRepository.getReferenceById(dto.getUserId()));
        newCapture.setSession(sessionRepository.getReferenceById(dto.getSessionId()));
        newCapture.setXpValue(10);

        capturedWordRepository.save(newCapture);

        return new CaptureResponseDTO(newCapture, true);
    }
}
