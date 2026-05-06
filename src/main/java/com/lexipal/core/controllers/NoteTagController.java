package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.NoteTagCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteTagUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteTagResponseDTO;
import com.lexipal.core.dtos.responses.NoteTagUsageResponseDTO;
import com.lexipal.core.services.NoteTagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/note-tags")
public class NoteTagController
{
    private final NoteTagService noteTagService;

    @Autowired
    public NoteTagController(final NoteTagService noteTagService)
    {
        this.noteTagService = noteTagService;
    }

    @GetMapping
    public ResponseEntity<List<NoteTagUsageResponseDTO>> getTags(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(noteTagService.getTagsForUser(keycloakSub));
    }

    @PostMapping
    public ResponseEntity<NoteTagResponseDTO> createTag(@Valid @RequestBody NoteTagCreateRequestDTO requestDTO,
                                                        @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteTagService.createTag(requestDTO, keycloakSub));
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<NoteTagResponseDTO> updateTag(@PathVariable Long tagId,
                                                        @Valid @RequestBody NoteTagUpdateRequestDTO requestDTO,
                                                        @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(noteTagService.updateTag(tagId, requestDTO, keycloakSub));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId,
                                          @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        noteTagService.deleteTag(tagId, keycloakSub);
        return ResponseEntity.noContent().build();
    }
}
