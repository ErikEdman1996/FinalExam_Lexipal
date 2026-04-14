package com.lexipal.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class WordFrequencyService
{
    private static final Logger logger = LogManager.getLogger(WordFrequencyService.class);

    private static final String WORD_FREQUENCY_RESOURCE = "word-frequency.csv";
    private static final double COMMON_RARITY = 0.30;
    private static final double UNCOMMON_RARITY = 0.55;
    private static final double RARE_RARITY = 0.75;
    private static final double MYTHIC_RARITY = 0.95;

    private static final double COMMON_BUCKET_CUTOFF = 0.50;
    private static final double UNCOMMON_BUCKET_CUTOFF = 0.80;
    private static final double RARE_BUCKET_CUTOFF = 0.95;

    private final Map<String, Integer> lemmaRanks;
    private final int maxTrackedRank;

    public WordFrequencyService()
    {
        Map<String, Integer> loadedRanks = loadLemmaRanks();
        this.lemmaRanks = Map.copyOf(loadedRanks);
        this.maxTrackedRank = loadedRanks.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);

        logger.info("Loaded {} word frequency entries from {}", lemmaRanks.size(), WORD_FREQUENCY_RESOURCE);
    }

    public double resolveRarityScore(String lemma)
    {
        Integer rank = lemmaRanks.get(normalizeLemma(lemma));

        if (rank == null)
        {
            return RARE_RARITY;
        }

        if (maxTrackedRank <= 1)
        {
            return COMMON_RARITY;
        }

        double normalizedRank = (double) (rank - 1) / (maxTrackedRank - 1);

        if (normalizedRank < COMMON_BUCKET_CUTOFF)
        {
            return COMMON_RARITY;
        }

        if (normalizedRank < UNCOMMON_BUCKET_CUTOFF)
        {
            return UNCOMMON_RARITY;
        }

        if (normalizedRank < RARE_BUCKET_CUTOFF)
        {
            return RARE_RARITY;
        }

        return MYTHIC_RARITY;
    }

    private Map<String, Integer> loadLemmaRanks()
    {
        ClassPathResource resource = new ClassPathResource(WORD_FREQUENCY_RESOURCE);
        Map<String, Integer> ranks = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null)
            {
                String[] columns = line.split(",", 3);
                if (columns.length < 2)
                {
                    continue;
                }

                int rank = Integer.parseInt(columns[0].trim());
                String lemma = normalizeLemma(columns[1]);

                ranks.merge(lemma, rank, Math::min);
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Failed to load word frequency data from " + WORD_FREQUENCY_RESOURCE, ex);
        }

        return ranks;
    }

    private String normalizeLemma(String lemma)
    {
        if (lemma == null)
        {
            return "";
        }

        return lemma.trim().toLowerCase();
    }
}
