package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.DictionaryAPIRequestDTO;
import com.lexipal.core.entities.Word;
import com.lexipal.core.exceptions.ExternalServiceException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.WordRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WordService implements WordServiceInterface
{
    private static final Logger logger = LogManager.getLogger(WordService.class);

    private final WordRepository wordRepository;
    private final DictionaryClientService client;
    private final WordFrequencyService wordFrequencyService;

    @Autowired
    public WordService(final WordRepository wordRepository,
                       final DictionaryClientService client,
                       final WordFrequencyService wordFrequencyService)
    {
        this.wordRepository = wordRepository;
        this.client = client;
        this.wordFrequencyService = wordFrequencyService;
    }

    @Override
    @Transactional
    @Cacheable(
            cacheNames = "words",
            key = "#lemma == null ? null : #lemma.trim().toLowerCase()",
            condition = "#lemma != null && !#lemma.isBlank()",
            sync = true
    )
    public Word saveWordFromApi(String lemma)
    {
        String normalizedLemma = normalizeLemma(lemma);

        return wordRepository.findByLemma(normalizedLemma).orElseGet(() -> {
            DictionaryAPIRequestDTO dto = client.fetchWordFromApi(normalizedLemma);

            if (dto == null) {
                throw new ExternalServiceException("Could not retrieve word data for: " + normalizedLemma);
            }

            if (dto.getWord() == null || dto.getWord().isBlank()) {
                throw new ResourceNotFoundException("Word", "lemma", normalizedLemma);
            }

            Word newWord = new Word();
            newWord.setLemma(dto.getWord().toLowerCase());
            newWord.setSource("Free Dictionary API");

            newWord.setDefinitions(dto.getMeanings());
            newWord.setPhonetics(dto.getPhonetics());

            newWord.setRarityScore(wordFrequencyService.resolveRarityScore(newWord.getLemma()));

            Word savedWord = wordRepository.save(newWord);
            logger.info("Saved new word '{}' with id {}", savedWord.getLemma(), savedWord.getId());
            return savedWord;
        });
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
