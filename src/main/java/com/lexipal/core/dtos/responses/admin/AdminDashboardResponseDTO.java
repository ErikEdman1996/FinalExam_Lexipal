package com.lexipal.core.dtos.responses.admin;

import java.util.List;

public record AdminDashboardResponseDTO(
        AdminDashboardRangeDTO range,
        AdminDashboardSummaryDTO summary,
        AdminDashboardLifetimeDTO lifetime,
        List<AdminDashboardDailyPointDTO> dailySeries,
        List<AdminDashboardTopUserDTO> topUsers,
        List<AdminDashboardTopBookDTO> topBooks
)
{
}
