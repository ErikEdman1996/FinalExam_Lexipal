package com.lexipal.core.models;

public record BookCoverMatch(String coverImageUrl,
                             String coverSource,
                             String externalBookId,
                             String matchedTitle,
                             String matchedAuthor,
                             int confidence)
{
}
