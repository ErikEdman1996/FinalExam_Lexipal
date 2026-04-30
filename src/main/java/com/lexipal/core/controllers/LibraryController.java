package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.LibraryItemResponseDTO;
import com.lexipal.core.services.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/library")
public class LibraryController
{
    private final LibraryService libraryService;

    public LibraryController(final LibraryService libraryService)
    {
        this.libraryService = libraryService;
    }

    @GetMapping("/items")
    public ResponseEntity<List<LibraryItemResponseDTO>> getLibraryItems(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(libraryService.getLibraryItems(jwt.getSubject()));
    }
}
