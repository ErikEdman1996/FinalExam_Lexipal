package com.lexipal.core.services;

import com.lexipal.core.dtos.responses.CodexBoardPageResponseDTO;
import com.lexipal.core.dtos.responses.CodexBoardProgressResponseDTO;
import com.lexipal.core.dtos.responses.CodexBoardResponseDTO;
import com.lexipal.core.dtos.responses.CodexBoardSlotResponseDTO;
import com.lexipal.core.entities.CodexEntry;
import com.lexipal.core.entities.User;
import com.lexipal.core.models.CodexBoardWordDefinition;
import com.lexipal.core.repositories.CodexEntryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CodexBoardService
{
    private static final Logger logger = LogManager.getLogger(CodexBoardService.class);
    private static final String BOARD_RESOURCE = "codex-board-volume-1.csv";
    private static final String VOLUME_KEY = "volume-1";

    private final CodexEntryRepository codexEntryRepository;
    private final UserServiceInterface userService;
    private final Map<String, CodexBoardWordDefinition> boardWordsByLemma;
    private final Map<Integer, List<CodexBoardWordDefinition>> boardWordsByPage;

    public CodexBoardService(final CodexEntryRepository codexEntryRepository,
                             final UserServiceInterface userService)
    {
        this.codexEntryRepository = codexEntryRepository;
        this.userService = userService;

        List<CodexBoardWordDefinition> boardWords = loadBoardWords();
        this.boardWordsByLemma = boardWords.stream()
                .collect(Collectors.toUnmodifiableMap(CodexBoardWordDefinition::lemma, definition -> definition));
        this.boardWordsByPage = boardWords.stream()
                .collect(Collectors.groupingBy(
                        CodexBoardWordDefinition::pageNumber,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                                .sorted(Comparator.comparingInt(CodexBoardWordDefinition::slotNumber))
                                .toList())
                ));

        logger.info("Loaded {} codex board words from {}", boardWords.size(), BOARD_RESOURCE);
    }

    public Optional<CodexBoardWordDefinition> findByLemma(String lemma)
    {
        return Optional.ofNullable(boardWordsByLemma.get(normalizeLemma(lemma)));
    }

    public List<CodexBoardWordDefinition> getBoardWords()
    {
        return boardWordsByPage.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public CodexBoardResponseDTO getBoardForUser(String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);
        return buildBoardResponse(user);
    }

    public CodexBoardProgressResponseDTO getOverallProgressForUser(User user)
    {
        int capturedWords = getCapturedBoardEntriesByLemma(user).size();
        return new CodexBoardProgressResponseDTO(
                capturedWords,
                boardWordsByLemma.size(),
                calculateCompletionPercentage(capturedWords, boardWordsByLemma.size())
        );
    }

    public CodexBoardProgressResponseDTO getPageProgressForUser(User user, int pageNumber)
    {
        List<CodexBoardWordDefinition> pageWords = boardWordsByPage.getOrDefault(pageNumber, List.of());
        Map<String, CodexEntry> capturedByLemma = getCapturedBoardEntriesByLemma(user);
        int capturedWords = (int) pageWords.stream()
                .filter(definition -> capturedByLemma.containsKey(definition.lemma()))
                .count();

        return new CodexBoardProgressResponseDTO(
                capturedWords,
                pageWords.size(),
                calculateCompletionPercentage(capturedWords, pageWords.size())
        );
    }

    private CodexBoardResponseDTO buildBoardResponse(User user)
    {
        Map<String, CodexEntry> capturedByLemma = getCapturedBoardEntriesByLemma(user);
        List<CodexBoardPageResponseDTO> pages = new ArrayList<>();

        for (Map.Entry<Integer, List<CodexBoardWordDefinition>> pageEntry : boardWordsByPage.entrySet())
        {
            List<CodexBoardSlotResponseDTO> slots = new ArrayList<>();
            int capturedCount = 0;

            for (CodexBoardWordDefinition definition : pageEntry.getValue())
            {
                CodexEntry capturedEntry = capturedByLemma.get(definition.lemma());
                boolean isCaptured = capturedEntry != null;

                if (isCaptured)
                {
                    capturedCount++;
                }

                slots.add(new CodexBoardSlotResponseDTO(
                        definition.slotNumber(),
                        isCaptured,
                        isCaptured ? definition.lemma() : null,
                        isCaptured ? definition.rarityTier() : null,
                        isCaptured ? capturedEntry.getCreatedAt() : null
                ));
            }

            pages.add(new CodexBoardPageResponseDTO(
                    pageEntry.getKey(),
                    capturedCount,
                    pageEntry.getValue().size(),
                    calculateCompletionPercentage(capturedCount, pageEntry.getValue().size()),
                    slots
            ));
        }

        int capturedWords = capturedByLemma.size();

        return new CodexBoardResponseDTO(
                VOLUME_KEY,
                boardWordsByLemma.size(),
                capturedWords,
                calculateCompletionPercentage(capturedWords, boardWordsByLemma.size()),
                pages
        );
    }

    private Map<String, CodexEntry> getCapturedBoardEntriesByLemma(User user)
    {
        return codexEntryRepository.findAllByUser_Id(user.getId()).stream()
                .filter(entry -> entry.getWord() != null && boardWordsByLemma.containsKey(normalizeLemma(entry.getWord().getLemma())))
                .collect(Collectors.toMap(
                        entry -> normalizeLemma(entry.getWord().getLemma()),
                        entry -> entry,
                        (existing, ignored) -> existing,
                        LinkedHashMap::new
                ));
    }

    private List<CodexBoardWordDefinition> loadBoardWords()
    {
        ClassPathResource resource = new ClassPathResource(BOARD_RESOURCE);
        List<CodexBoardWordDefinition> definitions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
        {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] columns = line.split(",", 5);
                if (columns.length < 5)
                {
                    continue;
                }

                definitions.add(new CodexBoardWordDefinition(
                        VOLUME_KEY,
                        Integer.parseInt(columns[0].trim()),
                        Integer.parseInt(columns[1].trim()),
                        normalizeLemma(columns[2]),
                        normalizeTier(columns[3]),
                        Double.parseDouble(columns[4].trim())
                ));
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Failed to load codex board data from " + BOARD_RESOURCE, ex);
        }

        return definitions;
    }

    private double calculateCompletionPercentage(int capturedWords, int totalWords)
    {
        if (totalWords <= 0)
        {
            return 0.0;
        }

        return Math.round(((double) capturedWords / totalWords) * 10000.0) / 100.0;
    }

    private String normalizeLemma(String lemma)
    {
        if (lemma == null)
        {
            return "";
        }

        return lemma.trim().toLowerCase();
    }

    private String normalizeTier(String rarityTier)
    {
        if (rarityTier == null)
        {
            return "common";
        }

        return rarityTier.trim().toLowerCase();
    }
}
