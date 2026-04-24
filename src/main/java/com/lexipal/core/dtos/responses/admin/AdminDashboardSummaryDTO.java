package com.lexipal.core.dtos.responses.admin;

public record AdminDashboardSummaryDTO(
        long totalUsers,
        long newUsers,
        long activeUsers,
        long totalPagesRead,
        long completedBooks,
        long completedSessions,
        long totalMinutesRead,
        long capturedWords,
        long xpAwarded,
        long readingListsCreated,
        long readingListSubscriptions,
        long readingListCompletions
)
{
}
