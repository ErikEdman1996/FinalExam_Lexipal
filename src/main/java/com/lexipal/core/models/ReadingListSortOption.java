package com.lexipal.core.models;

import org.springframework.data.domain.Sort;

public enum ReadingListSortOption
{
    POPULAR,
    RECENT,
    OLDEST,
    TITLE_ASC;

    public static ReadingListSortOption from(String value)
    {
        if (value == null || value.isBlank())
        {
            return POPULAR;
        }

        try
        {
            return ReadingListSortOption.valueOf(value.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            return POPULAR;
        }
    }

    public Sort toSort()
    {
        return switch (this)
        {
            case POPULAR -> Sort.by(Sort.Direction.DESC, "starCount")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            case RECENT -> Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case TITLE_ASC -> Sort.by(Sort.Direction.ASC, "title")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }
}
