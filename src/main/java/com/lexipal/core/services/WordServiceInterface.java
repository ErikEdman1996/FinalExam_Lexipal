package com.lexipal.core.services;

import com.lexipal.core.entities.Word;

import java.util.List;

public interface WordServiceInterface
{
    Word saveWordFromApi(String lemma);
}
