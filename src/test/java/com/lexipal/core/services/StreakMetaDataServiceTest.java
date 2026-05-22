package com.lexipal.core.services;

import com.lexipal.core.repositories.StreakMetaDataRepository;
import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.entities.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreakMetaDataServiceTest
{
    @Test
    void resolveEffectiveCurrentStreak_countsTodayAndYesterday()
    {
        LocalDate today = LocalDate.of(2026, 5, 21);
        StreakMetaDataService service = service();

        assertEquals(8, service.resolveEffectiveCurrentStreak(8, today, today));
        assertEquals(8, service.resolveEffectiveCurrentStreak(8, today.minusDays(1), today));
    }

    @Test
    void resolveEffectiveCurrentStreak_zeroesInactiveDatesAndMissingValues()
    {
        LocalDate today = LocalDate.of(2026, 5, 21);
        StreakMetaDataService service = service();

        assertEquals(0, service.resolveEffectiveCurrentStreak(8, today.minusDays(2), today));
        assertEquals(0, service.resolveEffectiveCurrentStreak(8, today.plusDays(1), today));
        assertEquals(0, service.resolveEffectiveCurrentStreak(8, null, today));
        assertEquals(0, service.resolveEffectiveCurrentStreak(null, today, today));
        assertEquals(0, service.resolveEffectiveCurrentStreak(0, today, today));
    }

    @Test
    void reconcileMissedDays_consumesOneFreezeForOneMissedDay()
    {
        LocalDate today = LocalDate.of(2026, 5, 22);
        StreakMetaData streak = streak(8, 8, today.minusDays(2), 1);

        service().reconcileMissedDays(streak, today);

        assertEquals(8, streak.getCurrentStreak());
        assertEquals(0, streak.getAvailableStreakFreezes());
        assertEquals(today.minusDays(1), streak.getFreezeProtectedThroughDate());
    }

    @Test
    void reconcileMissedDays_coversConsecutiveMissesUntilFreezesRunOut()
    {
        LocalDate today = LocalDate.of(2026, 5, 23);
        StreakMetaData covered = streak(8, 8, today.minusDays(3), 2);
        StreakMetaData uncovered = streak(8, 8, today.minusDays(3), 1);

        StreakMetaDataService service = service();
        service.reconcileMissedDays(covered, today);
        service.reconcileMissedDays(uncovered, today);

        assertEquals(8, covered.getCurrentStreak());
        assertEquals(0, covered.getAvailableStreakFreezes());
        assertEquals(today.minusDays(1), covered.getFreezeProtectedThroughDate());
        assertEquals(0, uncovered.getCurrentStreak());
        assertEquals(0, uncovered.getAvailableStreakFreezes());
        assertEquals(today.minusDays(2), uncovered.getFreezeProtectedThroughDate());
    }

    @Test
    void recordActivity_continuesStreakAfterCoveredMisses()
    {
        LocalDate activityDate = LocalDate.of(2026, 5, 23);
        User user = new User();
        user.setId(42L);
        StreakMetaData streak = streak(8, 8, activityDate.minusDays(3), 2);
        streak.setUser(user);

        StreakMetaDataRepository repository = mock(StreakMetaDataRepository.class);
        when(repository.findByUser_Id(user.getId())).thenReturn(Optional.of(streak));
        StreakMetaDataService service = new StreakMetaDataService(
                repository,
                mock(UserServiceInterface.class),
                "Europe/Stockholm"
        );

        service.recordActivity(user, activityDate);

        assertEquals(9, streak.getCurrentStreak());
        assertEquals(9, streak.getLongestStreak());
        assertEquals(activityDate, streak.getLastActivityDate());
        assertEquals(0, streak.getAvailableStreakFreezes());
        assertEquals(activityDate.minusDays(1), streak.getFreezeProtectedThroughDate());
    }

    private StreakMetaDataService service()
    {
        return new StreakMetaDataService(
                mock(StreakMetaDataRepository.class),
                mock(UserServiceInterface.class),
                "Europe/Stockholm"
        );
    }

    private StreakMetaData streak(int current, int longest, LocalDate lastActivityDate, int freezes)
    {
        StreakMetaData streak = new StreakMetaData();
        streak.setCurrentStreak(current);
        streak.setLongestStreak(longest);
        streak.setLastActivityDate(lastActivityDate);
        streak.setAvailableStreakFreezes(freezes);
        return streak;
    }
}
