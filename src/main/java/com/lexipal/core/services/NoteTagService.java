package com.lexipal.core.services;

import com.lexipal.core.dtos.requests.NoteTagCreateRequestDTO;
import com.lexipal.core.dtos.requests.NoteTagUpdateRequestDTO;
import com.lexipal.core.dtos.responses.NoteTagResponseDTO;
import com.lexipal.core.dtos.responses.NoteTagUsageResponseDTO;
import com.lexipal.core.entities.CatalogBook;
import com.lexipal.core.entities.NoteTag;
import com.lexipal.core.entities.User;
import com.lexipal.core.exceptions.ResourceConflictException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.exceptions.UnauthorizedActionException;
import com.lexipal.core.repositories.NoteTagAssignmentRepository;
import com.lexipal.core.repositories.NoteTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Transactional
public class NoteTagService
{
    private static final int MAX_TAG_NAME_LENGTH = 50;
    private static final Pattern COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private final NoteTagRepository noteTagRepository;
    private final NoteTagAssignmentRepository noteTagAssignmentRepository;
    private final UserServiceInterface userService;

    @Autowired
    public NoteTagService(final NoteTagRepository noteTagRepository,
                          final NoteTagAssignmentRepository noteTagAssignmentRepository,
                          final UserServiceInterface userService)
    {
        this.noteTagRepository = noteTagRepository;
        this.noteTagAssignmentRepository = noteTagAssignmentRepository;
        this.userService = userService;
    }

    public List<NoteTagUsageResponseDTO> getTagsForUser(String keycloakSub)
    {
        userService.getRequiredUser(keycloakSub);
        return noteTagRepository.findAllWithUsageCountsByUserSub(keycloakSub);
    }

    public NoteTagResponseDTO createTag(NoteTagCreateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);
        TagName tagName = validateName(dto.getName());
        assertNameAvailable(user.getId(), tagName.normalized(), null);

        NoteTag tag = new NoteTag();
        tag.setUser(user);
        tag.setName(tagName.display());
        tag.setColorHex(validateColor(dto.getColorHex()));

        return toResponse(noteTagRepository.save(tag));
    }

    public NoteTagResponseDTO updateTag(Long tagId, NoteTagUpdateRequestDTO dto, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);
        NoteTag tag = loadOwnedTag(tagId, user);

        if (dto.getName() != null)
        {
            TagName tagName = validateName(dto.getName());
            assertNameAvailable(user.getId(), tagName.normalized(), tag.getId());
            tag.setName(tagName.display());
        }

        if (dto.isColorHexProvided())
        {
            tag.setColorHex(validateColor(dto.getColorHex()));
        }

        return toResponse(noteTagRepository.save(tag));
    }

    public void deleteTag(Long tagId, String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);
        NoteTag tag = loadOwnedTag(tagId, user);

        noteTagAssignmentRepository.deleteByTagId(tag.getId());
        noteTagRepository.delete(tag);
    }

    private NoteTag loadOwnedTag(Long tagId, User user)
    {
        NoteTag tag = noteTagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("NoteTag", "id", tagId));

        if (!tag.getUser().getId().equals(user.getId()))
        {
            throw new UnauthorizedActionException("This tag does not belong to you.");
        }

        return tag;
    }

    private void assertNameAvailable(Long userId, String normalizedName, Long currentTagId)
    {
        noteTagRepository.findByUser_IdAndNameNormalized(userId, normalizedName)
                .filter(existing -> currentTagId == null || !existing.getId().equals(currentTagId))
                .ifPresent(existing -> {
                    throw new ResourceConflictException("A tag with this name already exists.");
                });
    }

    private TagName validateName(String rawName)
    {
        if (rawName == null || rawName.isBlank())
        {
            throw new IllegalArgumentException("Tag name cannot be blank.");
        }

        String displayName = rawName.trim();
        if (displayName.length() > MAX_TAG_NAME_LENGTH)
        {
            throw new IllegalArgumentException("Tag name must be " + MAX_TAG_NAME_LENGTH + " characters or fewer.");
        }

        String normalizedName = CatalogBook.normalize(displayName);
        if (normalizedName == null || normalizedName.isBlank())
        {
            throw new IllegalArgumentException("Tag name cannot be blank.");
        }

        return new TagName(displayName, normalizedName);
    }

    private String validateColor(String rawColor)
    {
        if (rawColor == null || rawColor.isBlank())
        {
            return null;
        }

        String color = rawColor.trim();
        if (!COLOR_PATTERN.matcher(color).matches())
        {
            throw new IllegalArgumentException("Color must be a hex value in #RRGGBB format.");
        }

        return color.toUpperCase(Locale.ROOT);
    }

    private NoteTagResponseDTO toResponse(NoteTag tag)
    {
        return new NoteTagResponseDTO(tag.getId(), tag.getName(), tag.getColorHex());
    }

    private record TagName(String display, String normalized)
    {

    }
}
