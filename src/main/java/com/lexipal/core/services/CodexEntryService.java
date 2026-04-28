package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.CodexEntryReadResponseDTO;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.User;
import com.lexipal.core.models.CodexBoardWordDefinition;
import com.lexipal.core.repositories.CodexEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CodexEntryService implements CodexEntryServiceInterface
{
    private final CodexEntryRepository codexEntryRepository;
    private final UserServiceInterface userService;
    private final CodexBoardService codexBoardService;

    public CodexEntryService(final CodexEntryRepository codexEntryRepository,
                             final UserServiceInterface userService,
                             final CodexBoardService codexBoardService)
    {
        this.codexEntryRepository = codexEntryRepository;
        this.userService = userService;
        this.codexBoardService = codexBoardService;
    }

    @Override
    public List<CodexEntryReadResponseDTO> getAllCodexEntriesBySub(String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);

        List<CodexEntry> words = codexEntryRepository.findAllBySub(user.getKeycloakSub());

        return words.stream()
                .map(codexEntry -> toCodexEntryReadResponse(
                        codexEntry,
                        codexBoardService.findByLemma(codexEntry.getWord().getLemma()).orElse(null)
                ))
                .collect(Collectors.toList());
    }

    private CodexEntryReadResponseDTO toCodexEntryReadResponse(CodexEntry codexEntry, CodexBoardWordDefinition boardWord)
    {
        return new CodexEntryReadResponseDTO(
                codexEntry.getId(),
                codexEntry.getWord().getId(),
                codexEntry.getSourceSessionId(),
                codexEntry.getWord().getLemma(),
                codexEntry.getSpecificDefinition(),
                codexEntry.getXpValue(),
                codexEntry.getCreatedAt(),
                boardWord != null ? boardWord.rarityScore() : codexEntry.getWord().getRarityScore(),
                boardWord != null ? boardWord.rarityTier() : null,
                codexEntry.getWord().getSource(),
                codexEntry.getWord().getDefinitions(),
                codexEntry.getWord().getPhonetics()
        );
    }
}
