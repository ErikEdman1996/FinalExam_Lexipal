package com.lexipal.core.dtos.responses.admin;

public record AdminDashboardTopUserDTO(
        long userId,
        String nickname,
        long totalPagesRead,
        long completedBooks,
        long completedSessions,
        long totalXpEarned
)
{
}
