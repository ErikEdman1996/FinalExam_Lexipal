package com.lexipal.core.services;

import com.lexipal.core.dtos.DictionaryAPIRequestDTO;
import com.lexipal.core.entities.Word;
import com.lexipal.core.repositories.WordRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WordService implements WordServiceInterface
{
    private final WordRepository wordRepository;
    private final DictionaryClientService client;

    @Autowired
    public WordService(final WordRepository wordRepository,
                       final DictionaryClientService client)
    {
        this.wordRepository = wordRepository;
        this.client = client;
    }

    @Override
    @Transactional
    public Word saveWordFromApi(String lemma)
    {
        // 1. Check if we already have this word in our database to avoid duplicates
        return wordRepository.findByLemma(lemma).orElseGet(() -> {

            // 2. Fetch the data from the external Dictionary API
            DictionaryAPIRequestDTO dto = client.fetchWordFromApi(lemma);

            if (dto == null) {
                throw new RuntimeException("Could not retrieve word data for: " + lemma);
            }

            // 3. Map the DTO to our Entity
            Word newWord = new Word();
            newWord.setLemma(dto.getWord().toLowerCase());
            newWord.setSource("Free Dictionary API");

            // Setting the JSONB fields directly from the DTO's JsonNodes
            newWord.setDefinitions(dto.getMeanings());
            newWord.setPhonetics(dto.getPhonetics());

            // 4. Calculate Rarity (Example logic for your Exam Project)
            // You could base this on word length or a lookup table later
            newWord.setRarityScore(calculateRarity(dto.getWord()));

            // 5. Save and return
            return wordRepository.save(newWord);
        });
    }

    private Double calculateRarity(String word) {
        // Simple placeholder logic for your "Pokemon-style" rarity
        // In a real app, you might check this against a frequency list
        if (word.length() > 10) return 0.85; // Rare
        if (word.length() > 7) return 0.50;  // Uncommon
        return 0.15; // Common
    }
}
