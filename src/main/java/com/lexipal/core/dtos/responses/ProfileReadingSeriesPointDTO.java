package com.lexipal.core.dtos.responses;

import java.time.LocalDate;

public record ProfileReadingSeriesPointDTO(
        LocalDate date,
        long pagesRead
)
{
}
