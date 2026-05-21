package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.StreakLeaderboardEntryResponseDTO;
import com.lexipal.core.dtos.responses.StreakLeaderboardResponseDTO;
import com.lexipal.core.repositories.StreakMetaDataRepository;
import com.lexipal.core.repositories.projections.StreakLeaderboardEntryProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaderboardService
{
    private static final int DEFAULT_LIMIT = 10;

    private final StreakMetaDataRepository streakMetaDataRepository;
    private final ZoneId streakZone;

    public LeaderboardService(final StreakMetaDataRepository streakMetaDataRepository,
                              @Value("${lexipal.streak.timezone:Europe/Stockholm}") String timezone)
    {
        this.streakMetaDataRepository = streakMetaDataRepository;
        this.streakZone = ZoneId.of(timezone);
    }

    public StreakLeaderboardResponseDTO getStreakLeaderboard(Integer limit)
    {
        int resolvedLimit = resolveLimit(limit);
        LocalDate asOfDate = LocalDate.now(streakZone);
        LocalDate previousDate = asOfDate.minusDays(1);

        List<StreakLeaderboardEntryProjection> rows =
                streakMetaDataRepository.findStreakLeaderboard(asOfDate, previousDate, resolvedLimit);

        List<StreakLeaderboardEntryResponseDTO> entries = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++)
        {
            StreakLeaderboardEntryProjection row = rows.get(i);
            entries.add(new StreakLeaderboardEntryResponseDTO(
                    i + 1,
                    row.getUserId(),
                    row.getNickname(),
                    row.getCurrentStreak() == null ? 0 : row.getCurrentStreak(),
                    row.getLastActivityDate()
            ));
        }

        return new StreakLeaderboardResponseDTO(
                resolvedLimit,
                asOfDate,
                streakZone.getId(),
                entries
        );
    }

    private int resolveLimit(Integer limit)
    {
        if (limit == null)
        {
            return DEFAULT_LIMIT;
        }

        if (limit == 10 || limit == 50 || limit == 100)
        {
            return limit;
        }

        throw new IllegalArgumentException("limit must be one of 10, 50, or 100.");
    }
}
