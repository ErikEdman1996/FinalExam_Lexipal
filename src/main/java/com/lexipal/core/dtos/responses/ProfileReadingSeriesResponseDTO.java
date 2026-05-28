package com.lexipal.core.dtos.responses;

import java.time.LocalDate;
import java.util.List;

public record ProfileReadingSeriesResponseDTO(
        ProfileReadingSeriesRange range,
        LocalDate startDate,
        LocalDate endDate,
        String timezone,
        ReadingSeriesBucketType bucketType,
        List<ProfileReadingSeriesPointDTO> points
)
{
}
