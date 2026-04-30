package com.lexipal.core.configs;

import com.lexipal.core.entities.Challenge;
import com.lexipal.core.entities.ChallengeDifficulty;
import com.lexipal.core.entities.ChallengeType;
import com.lexipal.core.repositories.ChallengeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChallengeDataInitializer
{
    @Bean
    CommandLineRunner seedDefaultChallenges(ChallengeRepository challengeRepository)
    {
        return args -> {
            List<DefaultChallenge> defaultChallenges = List.of(
                    new DefaultChallenge(
                            ChallengeType.FLASHCARD,
                            "Review a word by recalling its meaning from memory.",
                            ChallengeDifficulty.EASY
                    ),
                    new DefaultChallenge(
                            ChallengeType.CROSSWORD,
                            "Solve a five-word crossword built from your vocabulary.",
                            ChallengeDifficulty.MEDIUM
                    )
            );

            for (DefaultChallenge defaultChallenge : defaultChallenges)
            {
                challengeRepository.findByType(defaultChallenge.type())
                        .orElseGet(() -> {
                            Challenge challenge = new Challenge();
                            challenge.setType(defaultChallenge.type());
                            challenge.setDescription(defaultChallenge.description());
                            challenge.setDifficulty(defaultChallenge.difficulty());
                            return challengeRepository.save(challenge);
                        });
            }
        };
    }

    private record DefaultChallenge(ChallengeType type, String description, ChallengeDifficulty difficulty)
    {
    }
}
