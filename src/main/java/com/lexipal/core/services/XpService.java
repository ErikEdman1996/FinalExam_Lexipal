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
    private static final int XP_PER_LEVEL = 100;

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
                XP_PER_LEVEL,
                newLevel > previousLevel
        );
    }

    public int calculateLevel(int totalXp)
    {
        return (totalXp / XP_PER_LEVEL) + 1;
    }

    private int calculateXpIntoLevel(int totalXp)
    {
        return totalXp % XP_PER_LEVEL;
    }
}
