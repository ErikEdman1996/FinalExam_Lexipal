package com.lexipal.core.controllers;

import com.lexipal.core.entities.Word;
import com.lexipal.core.services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/dictionary")
public class TestController
{
    private final WordService wordService;

    @Autowired
    public TestController(final WordService wordService)
    {
        this.wordService = wordService;
    }

    @GetMapping("/{lemma}")
    public ResponseEntity<Word> getWord(@PathVariable String lemma)
    {
        Word word = wordService.saveWordFromApi(lemma);

        return ResponseEntity.ok(word);
    }
}
