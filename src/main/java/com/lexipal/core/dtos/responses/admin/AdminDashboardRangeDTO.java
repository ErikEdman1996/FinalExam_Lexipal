package com.lexipal.core.dtos.responses.admin;

import java.time.LocalDate;

public record AdminDashboardRangeDTO(
        AdminAnalyticsRange key,
        LocalDate startDate,
        LocalDate endDate,
        String timezone
)
{
}
