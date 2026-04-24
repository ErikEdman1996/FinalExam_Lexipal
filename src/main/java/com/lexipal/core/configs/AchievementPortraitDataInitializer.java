package com.lexipal.core.configs;

import com.lexipal.core.entities.AchievementDefinition;
import com.lexipal.core.entities.PortraitDefinition;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.AchievementDefinitionRepository;
import com.lexipal.core.repositories.PortraitDefinitionRepository;
import com.lexipal.core.repositories.UserRepository;
import com.lexipal.core.services.AchievementService;
import com.lexipal.core.services.PortraitService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

@Configuration
public class AchievementPortraitDataInitializer
{
    @Bean
    @Order(1)
    CommandLineRunner seedAchievementDefinitions(AchievementDefinitionRepository repository)
    {
        return args -> {
            seedAchievementSeries(repository, "PAGES", "Pages read", AchievementService.METRIC_PAGES_READ,
                    "Read %,d pages", "Read %,d total pages.", 100, 50, 100, 200, 500, 1000, 2000, 5000);
            seedAchievementSeries(repository, "BOOKS", "Books completed", AchievementService.METRIC_BOOKS_COMPLETED,
                    "Complete %,d books", "Complete %,d books.", 200, 1, 3, 5, 10, 25, 50, 100);
            seedAchievementSeries(repository, "CODEX", "Codex words", AchievementService.METRIC_CODEX_WORDS,
                    "Find %,d codex words", "Add %,d words to your codex.", 300, 10, 25, 50, 100, 200, 300);
            seedAchievementSeries(repository, "LIST_STARS", "Reading list stars", AchievementService.METRIC_READING_LIST_STARS,
                    "Earn %,d list stars", "Earn %,d stars on one reading list.", 400, 1, 5, 10, 25, 50, 100);
            seedAchievementSeries(repository, "LIST_SUBSCRIBERS", "Reading list subscribers",
                    AchievementService.METRIC_READING_LIST_SUBSCRIBERS,
                    "Earn %,d list subscribers", "Earn %,d subscribers on one reading list.", 500, 1, 5, 10, 25, 50, 100);
            seedAchievementSeries(repository, "CIRCLES_STARTED", "Book circles started",
                    AchievementService.METRIC_BOOK_CIRCLES_STARTED,
                    "Start %,d book circles", "Start %,d book circles.", 600, 1, 3, 5, 10);
            seedAchievementSeries(repository, "CIRCLE_BOOKS", "Book-circle books completed",
                    AchievementService.METRIC_BOOK_CIRCLE_BOOKS_COMPLETED,
                    "Complete %,d circle books", "Complete %,d books connected to book circles.", 700, 1, 3, 5, 10);
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner seedPortraitDefinitions(PortraitDefinitionRepository repository)
    {
        return args -> {
            seedPortrait(repository, "larvae_starter", "Starter Larvae",
                    "A small bright larvae beginning its Lexipal journey.", "mascot_larvae_starter", 1, 10);
            seedPortrait(repository, "curious_larvae", "Curious Larvae",
                    "A curious larvae growing bolder with every page.", "mascot_curious_larvae", 3, 20);
            seedPortrait(repository, "silken_cocoon", "Silken Cocoon",
                    "A quiet cocoon holding all that reading energy.", "mascot_silken_cocoon", 5, 30);
            seedPortrait(repository, "emerging_moth", "Emerging Moth",
                    "The first soft wings appear.", "mascot_emerging_moth", 8, 40);
            seedPortrait(repository, "young_moth", "Young Moth",
                    "A young moth ready for longer flights.", "mascot_young_moth", 10, 50);
            seedPortrait(repository, "lexipal_moth", "Lexipal Moth",
                    "The full site mascot, glowing with reading progress.", "mascot_lexipal_moth", 15, 60);
        };
    }

    @Bean
    @DependsOn({"seedAchievementDefinitions", "seedPortraitDefinitions"})
    @Order(2)
    CommandLineRunner backfillAchievementsAndPortraits(UserRepository userRepository,
                                                       AchievementService achievementService,
                                                       PortraitService portraitService)
    {
        return args -> {
            for (User user : userRepository.findAll())
            {
                achievementService.evaluateAchievements(user);
                portraitService.unlockEligiblePortraits(user);
            }
        };
    }

    private void seedAchievementSeries(AchievementDefinitionRepository repository, String codePrefix,
                                       String category, String metricKey, String titlePattern,
                                       String descriptionPattern, int sortBase, int... thresholds)
    {
        for (int i = 0; i < thresholds.length; i++)
        {
            int threshold = thresholds[i];
            String code = codePrefix + "_" + threshold;
            if (repository.findByCode(code).isPresent())
            {
                continue;
            }

            AchievementDefinition definition = new AchievementDefinition();
            definition.setCode(code);
            definition.setCategory(category);
            definition.setTitle(String.format(titlePattern, threshold));
            definition.setDescription(String.format(descriptionPattern, threshold));
            definition.setMetricKey(metricKey);
            definition.setThreshold(threshold);
            definition.setSortOrder(sortBase + i);
            definition.setActive(true);
            repository.save(definition);
        }
    }

    private void seedPortrait(PortraitDefinitionRepository repository, String code, String name,
                              String description, String assetKey, int requiredLevel, int sortOrder)
    {
        if (repository.findByCode(code).isPresent())
        {
            return;
        }

        PortraitDefinition portrait = new PortraitDefinition();
        portrait.setCode(code);
        portrait.setName(name);
        portrait.setDescription(description);
        portrait.setAssetKey(assetKey);
        portrait.setRequiredLevel(requiredLevel);
        portrait.setSortOrder(sortOrder);
        portrait.setActive(true);
        repository.save(portrait);
    }
}
