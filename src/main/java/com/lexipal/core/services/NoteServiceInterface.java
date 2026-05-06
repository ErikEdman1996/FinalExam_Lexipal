package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.NoteCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteReadResponseDTO;
import com.lexipal.core.models.NoteTagMatch;

import java.util.List;

public interface NoteServiceInterface
{
    NoteReadResponseDTO createNote(NoteCreateRequestDTO dto, String keycloakSub);

    List<NoteReadResponseDTO> getAllNotesForUser(String keycloakSub, List<Long> tagIds, NoteTagMatch tagMatch);

    List<NoteReadResponseDTO> getAllNotesForBook(Long bookId, String keycloakSub, List<Long> tagIds,
                                                 NoteTagMatch tagMatch);

    NoteReadResponseDTO updateNote(Long noteId, NoteUpdateRequestDTO dto, String keycloakSub);

    void deleteNote(Long noteId, String keycloakSub);
}
