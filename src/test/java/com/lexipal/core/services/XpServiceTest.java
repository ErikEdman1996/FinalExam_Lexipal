package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.DailyActivityRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class XpServiceTest
{
    @ParameterizedTest
    @MethodSource("thresholdProgressCases")
    void awardXp_returnsThresholdProgressForBoundaryXp(int totalXp, int expectedLevel, int expectedXpIntoLevel,
                                                       int expectedXpPerLevel, int expectedCurrentLevelMinXp,
                                                       Integer expectedNextLevelMinXp, int expectedXpToNextLevel)
    {
        XpUpdateResponseDTO update = service().awardXp(userWithTotalXp(totalXp), 0);

        assertEquals(totalXp, update.getNewTotalXp());
        assertEquals(expectedLevel, update.getLevel());
        assertEquals(expectedXpIntoLevel, update.getXpIntoLevel());
        assertEquals(expectedXpPerLevel, update.getXpPerLevel());
        assertEquals(XpService.MAX_LEVEL, update.getMaxLevel());
        assertEquals(expectedCurrentLevelMinXp, update.getCurrentLevelMinXp());

        if (expectedNextLevelMinXp == null)
        {
            assertNull(update.getNextLevelMinXp());
        }
        else
        {
            assertEquals(expectedNextLevelMinXp, update.getNextLevelMinXp());
        }

        assertEquals(expectedXpToNextLevel, update.getXpToNextLevel());
    }

    @Test
    void calculateLevel_clampsNegativeXpToLevelOne()
    {
        assertEquals(1, service().calculateLevel(-50));
    }

    @Test
    void awardXp_marksMultiLevelJumpAsLeveledUp()
    {
        XpUpdateResponseDTO update = service().awardXp(userWithTotalXp(90), 700);

        assertEquals(790, update.getNewTotalXp());
        assertEquals(5, update.getLevel());
        assertTrue(update.isLeveledUp());
    }

    private static Stream<Arguments> thresholdProgressCases()
    {
        return Stream.of(
                Arguments.of(0, 1, 0, 100, 0, 100, 100),
                Arguments.of(99, 1, 99, 100, 0, 100, 1),
                Arguments.of(100, 2, 0, 150, 100, 250, 150),
                Arguments.of(249, 2, 149, 150, 100, 250, 1),
                Arguments.of(250, 3, 0, 200, 250, 450, 200),
                Arguments.of(2699, 9, 499, 500, 2200, 2700, 1),
                Arguments.of(2700, 10, 500, 500, 2700, null, 0),
                Arguments.of(3200, 10, 500, 500, 2700, null, 0)
        );
    }

    private XpService service()
    {
        return new XpService(mock(UserRepository.class), mock(DailyActivityRepository.class));
    }

    private User userWithTotalXp(int totalXp)
    {
        User user = new User();
        user.setTotalXp(totalXp);
        return user;
    }
}
