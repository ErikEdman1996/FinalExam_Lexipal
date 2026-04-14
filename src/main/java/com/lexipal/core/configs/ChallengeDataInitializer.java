package com.lexipal.core.configs;

import com.lexipal.core.entities.Challenge;
import com.lexipal.core.entities.ChallengeDifficulty;
import com.lexipal.core.entities.ChallengeType;
import com.lexipal.core.repositories.ChallengeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChallengeDataInitializer
{
    @Bean
    CommandLineRunner seedDefaultChallenge(ChallengeRepository challengeRepository)
    {
        return args -> challengeRepository.findByType(ChallengeType.FLASHCARD)
                .orElseGet(() -> {
                    Challenge challenge = new Challenge();
                    challenge.setType(ChallengeType.FLASHCARD);
                    challenge.setDescription("Review a word by recalling its meaning from memory.");
                    challenge.setDifficulty(ChallengeDifficulty.EASY);
                    return challengeRepository.save(challenge);
                });
    }
}
