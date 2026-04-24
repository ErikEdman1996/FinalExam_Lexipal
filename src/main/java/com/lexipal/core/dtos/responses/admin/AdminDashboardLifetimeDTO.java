package com.lexipal.core.dtos.responses.admin;

public record AdminDashboardLifetimeDTO(
        long totalUsers,
        long totalBooks,
        long completedBooks,
        long totalPagesRead,
        long completedSessions,
        long totalXpAwarded
)
{
}
