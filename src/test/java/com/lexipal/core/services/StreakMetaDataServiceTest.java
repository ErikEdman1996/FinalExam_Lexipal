package com.lexipal.core.services;

import com.lexipal.core.repositories.StreakMetaDataRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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

    private StreakMetaDataService service()
    {
        return new StreakMetaDataService(
                mock(StreakMetaDataRepository.class),
                mock(UserServiceInterface.class),
                "Europe/Stockholm"
        );
    }
}
