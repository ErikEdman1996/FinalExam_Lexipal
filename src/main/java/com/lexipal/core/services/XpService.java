package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class XpService
{
    public static final int MAX_LEVEL = 10;
    private static final int[] LEVEL_XP_THRESHOLDS = {
            0,
            100,
            250,
            450,
            700,
            1000,
            1350,
            1750,
            2200,
            2700
    };

    private final UserRepository userRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final PortraitService portraitService;

    @Autowired
    public XpService(final UserRepository userRepository,
                     final DailyActivityRepository dailyActivityRepository,
                     @Lazy final PortraitService portraitService)
    {
        this.userRepository = userRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.portraitService = portraitService;
    }

    public XpService(final UserRepository userRepository,
                     final DailyActivityRepository dailyActivityRepository)
    {
        this.userRepository = userRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.portraitService = null;
    }

    public XpUpdateResponseDTO awardXp(User user, int xpAwarded)
    {
        int previousTotalXp = getCurrentTotalXp(user);
        int safeAward = Math.max(xpAwarded, 0);
        int newTotalXp = previousTotalXp + safeAward;

        user.setTotalXp(newTotalXp);
        userRepository.save(user);
        if (portraitService != null)
        {
            portraitService.unlockEligiblePortraits(user);
        }

        return buildXpUpdate(safeAward, previousTotalXp, newTotalXp);
    }

    public int getCurrentTotalXp(User user)
    {
        if (user.getTotalXp() == null)
        {
            int backfilledXp = dailyActivityRepository.sumPositiveXpEarnedByUserId(user.getId());
            user.setTotalXp(backfilledXp);
            userRepository.save(user);
        }

        return user.getTotalXp();
    }

    public XpUpdateResponseDTO buildZeroAwardUpdate(User user)
    {
        int currentTotalXp = getCurrentTotalXp(user);
        return buildXpUpdate(0, currentTotalXp, currentTotalXp);
    }

    private XpUpdateResponseDTO buildXpUpdate(int xpAwarded, int previousTotalXp, int newTotalXp)
    {
        int previousLevel = calculateLevel(previousTotalXp);
        int newLevel = calculateLevel(newTotalXp);

        return new XpUpdateResponseDTO(
                xpAwarded,
                previousTotalXp,
                newTotalXp,
                newLevel,
                calculateXpIntoLevel(newTotalXp),
                calculateXpPerLevel(newTotalXp),
                newLevel > previousLevel,
                MAX_LEVEL,
                calculateCurrentLevelMinXp(newTotalXp),
                calculateNextLevelMinXp(newTotalXp),
                calculateXpToNextLevel(newTotalXp)
        );
    }

    public int calculateLevel(int totalXp)
    {
        int normalizedXp = normalizeTotalXp(totalXp);

        for (int level = MAX_LEVEL; level >= 1; level--)
        {
            if (normalizedXp >= thresholdForLevel(level))
            {
                return level;
            }
        }

        return 1;
    }

    public int calculateXpIntoLevel(int totalXp)
    {
        int level = calculateLevel(totalXp);

        if (level == MAX_LEVEL)
        {
            return calculateXpPerLevel(totalXp);
        }

        return normalizeTotalXp(totalXp) - calculateCurrentLevelMinXp(totalXp);
    }

    public int calculateXpPerLevel(int totalXp)
    {
        int level = calculateLevel(totalXp);

        if (level == MAX_LEVEL)
        {
            return thresholdForLevel(MAX_LEVEL) - thresholdForLevel(MAX_LEVEL - 1);
        }

        return thresholdForLevel(level + 1) - thresholdForLevel(level);
    }

    public int calculateCurrentLevelMinXp(int totalXp)
    {
        return thresholdForLevel(calculateLevel(totalXp));
    }

    public Integer calculateNextLevelMinXp(int totalXp)
    {
        int level = calculateLevel(totalXp);

        if (level >= MAX_LEVEL)
        {
            return null;
        }

        return thresholdForLevel(level + 1);
    }

    public int calculateXpToNextLevel(int totalXp)
    {
        Integer nextLevelMinXp = calculateNextLevelMinXp(totalXp);

        if (nextLevelMinXp == null)
        {
            return 0;
        }

        return Math.max(nextLevelMinXp - normalizeTotalXp(totalXp), 0);
    }

    public int getMaxLevel()
    {
        return MAX_LEVEL;
    }

    private int thresholdForLevel(int level)
    {
        int clampedLevel = Math.max(1, Math.min(level, MAX_LEVEL));
        return LEVEL_XP_THRESHOLDS[clampedLevel - 1];
    }

    private int normalizeTotalXp(int totalXp)
    {
        return Math.max(totalXp, 0);
    }
}
