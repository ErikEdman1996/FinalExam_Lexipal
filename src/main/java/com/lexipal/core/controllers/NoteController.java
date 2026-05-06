package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.NoteCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteReadResponseDTO;
import com.lexipal.core.models.NoteTagMatch;
import com.lexipal.core.services.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/notes")
public class NoteController
{
    private final NoteService noteService;

    @Autowired
    public NoteController(final NoteService noteService)
    {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteReadResponseDTO> createNote(@Valid @RequestBody NoteCreateRequestDTO requestDTO,
                                                          @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        NoteReadResponseDTO response = noteService.createNote(requestDTO, keycloakSub);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NoteReadResponseDTO>> getAllNotes(@RequestParam(required = false) List<Long> tagIds,
                                                                 @RequestParam(defaultValue = "ANY") NoteTagMatch tagMatch,
                                                                 @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        List<NoteReadResponseDTO> notes = noteService.getAllNotesForUser(keycloakSub, tagIds, tagMatch);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<NoteReadResponseDTO>> getNotesForBook(@PathVariable Long bookId,
                                                                     @RequestParam(required = false) List<Long> tagIds,
                                                                     @RequestParam(defaultValue = "ANY") NoteTagMatch tagMatch,
                                                                     @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        List<NoteReadResponseDTO> notes = noteService.getAllNotesForBook(bookId, keycloakSub, tagIds, tagMatch);
        return ResponseEntity.ok(notes);
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<NoteReadResponseDTO> updateNote(@PathVariable Long noteId,
                                                          @Valid @RequestBody NoteUpdateRequestDTO requestDTO,
                                                          @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        NoteReadResponseDTO response = noteService.updateNote(noteId, requestDTO, keycloakSub);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId,
                                           @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        noteService.deleteNote(noteId, keycloakSub);
        return ResponseEntity.noContent().build();
    }
}
