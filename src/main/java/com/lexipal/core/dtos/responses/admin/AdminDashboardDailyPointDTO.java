package com.lexipal.core.dtos.responses.admin;

import java.time.LocalDate;

public record AdminDashboardDailyPointDTO(
        LocalDate date,
        long pagesRead,
        long completedSessions,
        long completedBooks,
        long newUsers,
        long activeUsers,
        long xpAwarded
)
{
}
