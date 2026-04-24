package com.lexipal.core.dtos.responses.admin;

public record AdminDashboardTopBookDTO(
        String title,
        String author,
        long completedCount,
        long pagesRead
)
{
}
