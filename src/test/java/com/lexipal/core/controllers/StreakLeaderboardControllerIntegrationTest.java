package com.lexipal.core.controllers;

import com.lexipal.core.IntegrationTestBase;
import com.lexipal.core.entities.StreakMetaData;
import com.lexipal.core.entities.User;
import com.lexipal.core.repositories.StreakMetaDataRepository;
import com.lexipal.core.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StreakLeaderboardControllerIntegrationTest extends IntegrationTestBase
{
    private static final String LEADERBOARD_URL = "/lexipal/api/v1/leaderboards/streaks";
    private static final ZoneId STREAK_ZONE = ZoneId.of("Europe/Stockholm");

    @Autowired private UserRepository userRepository;
    @Autowired private StreakMetaDataRepository streakMetaDataRepository;

    @Test
    @DisplayName("GET /leaderboards/streaks accepts only supported leaderboard limits")
    void getStreakLeaderboard_acceptsSupportedLimits() throws Exception
    {
        User user = createUser("leaderboard-limits-sub", "Limit Reader");
        createStreak(user, 4, 6, LocalDate.now(STREAK_ZONE));

        mockMvc.perform(get(LEADERBOARD_URL).with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.entries", hasSize(1)));

        mockMvc.perform(get(LEADERBOARD_URL + "?limit=50").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(50))
                .andExpect(jsonPath("$.entries", hasSize(1)));

        mockMvc.perform(get(LEADERBOARD_URL + "?limit=100").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(100))
                .andExpect(jsonPath("$.entries", hasSize(1)));
    }

    @Test
    @DisplayName("GET /leaderboards/streaks rejects unsupported limits")
    void getStreakLeaderboard_rejectsUnsupportedLimit() throws Exception
    {
        mockMvc.perform(get(LEADERBOARD_URL + "?limit=25").with(jwtWithSub("leaderboard-invalid-limit-sub")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("limit must be one of 10, 50, or 100."));
    }

    @Test
    @DisplayName("GET /leaderboards/streaks filters unnamed and inactive users and zeroes stale streaks")
    void getStreakLeaderboard_filtersUsersAndZeroesStaleStreaks() throws Exception
    {
        LocalDate today = LocalDate.now(STREAK_ZONE);
        User fresh = createUser("leaderboard-fresh-sub", "Fresh Reader");
        User stale = createUser("leaderboard-stale-sub", "Stale Reader");
        User noStreak = createUser("leaderboard-no-streak-sub", "No Streak Reader");
        User unnamed = createUser("leaderboard-unnamed-sub", null);
        User inactive = createUser("leaderboard-inactive-sub", "Inactive Reader");
        inactive.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(inactive);

        createStreak(fresh, 3, 3, today);
        createStreak(stale, 100, 100, today.minusDays(2));
        createStreak(unnamed, 200, 200, today);
        createStreak(inactive, 300, 300, today);

        mockMvc.perform(get(LEADERBOARD_URL).with(jwtWithSub(fresh.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value(STREAK_ZONE.getId()))
                .andExpect(jsonPath("$.entries", hasSize(3)))
                .andExpect(jsonPath("$.entries[0].nickname").value("Fresh Reader"))
                .andExpect(jsonPath("$.entries[0].currentStreak").value(3))
                .andExpect(jsonPath("$.entries[?(@.nickname == 'Stale Reader')].currentStreak").value(hasItem(0)))
                .andExpect(jsonPath("$.entries[?(@.nickname == 'No Streak Reader')].currentStreak").value(hasItem(0)))
                .andExpect(jsonPath("$.entries[?(@.nickname == 'Inactive Reader')]").isEmpty())
                .andExpect(jsonPath("$.entries[?(@.nickname == null)]").isEmpty());
    }

    @Test
    @DisplayName("GET /leaderboards/streaks orders by effective streak, recency, then user id")
    void getStreakLeaderboard_ordersDeterministically() throws Exception
    {
        LocalDate today = LocalDate.now(STREAK_ZONE);
        User yesterday = createUser("leaderboard-yesterday-sub", "Yesterday Tie");
        User todayLowId = createUser("leaderboard-today-low-sub", "Today Low Id");
        User todayHighId = createUser("leaderboard-today-high-sub", "Today High Id");

        createStreak(yesterday, 5, 5, today.minusDays(1));
        createStreak(todayLowId, 5, 5, today);
        createStreak(todayHighId, 5, 5, today);

        mockMvc.perform(get(LEADERBOARD_URL).with(jwtWithSub(todayLowId.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[0].nickname").value("Today Low Id"))
                .andExpect(jsonPath("$.entries[1].rank").value(2))
                .andExpect(jsonPath("$.entries[1].nickname").value("Today High Id"))
                .andExpect(jsonPath("$.entries[2].rank").value(3))
                .andExpect(jsonPath("$.entries[2].nickname").value("Yesterday Tie"));
    }

    @Test
    @DisplayName("Existing streak reads return effective current streaks")
    void existingStreakReads_returnEffectiveCurrentStreaks() throws Exception
    {
        LocalDate today = LocalDate.now(STREAK_ZONE);
        User user = createUser("leaderboard-existing-streak-sub", "Profile Stale");
        createStreak(user, 12, 12, today.minusDays(2));

        mockMvc.perform(get("/lexipal/api/v1/streakMetadata/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(0))
                .andExpect(jsonPath("$.longestStreak").value(12));

        mockMvc.perform(get("/lexipal/api/v1/profile/me").with(jwtWithSub(user.getKeycloakSub())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streak.currentStreak").value(0))
                .andExpect(jsonPath("$.streak.longestStreak").value(12));
    }

    private User createUser(String sub, String nickname)
    {
        User user = new User();
        user.setKeycloakSub(sub);
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    private StreakMetaData createStreak(User user, int currentStreak, int longestStreak, LocalDate lastActivityDate)
    {
        StreakMetaData streakMetaData = new StreakMetaData();
        streakMetaData.setUser(user);
        streakMetaData.setCurrentStreak(currentStreak);
        streakMetaData.setLongestStreak(longestStreak);
        streakMetaData.setLastActivityDate(lastActivityDate);
        return streakMetaDataRepository.save(streakMetaData);
    }
}
