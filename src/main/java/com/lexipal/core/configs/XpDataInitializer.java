package com.lexipal.core.configs;

import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XpDataInitializer
{
    @Bean
    CommandLineRunner backfillUserTotalXp(UserRepository userRepository,
                                          DailyActivityRepository dailyActivityRepository)
    {
        return args -> {
            for (User user : userRepository.findByTotalXpIsNull())
            {
                int totalXp = dailyActivityRepository.sumPositiveXpEarnedByUserId(user.getId());
                user.setTotalXp(totalXp);
                userRepository.save(user);
            }
        };
    }
}
