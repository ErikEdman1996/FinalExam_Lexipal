package com.lexipal.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexipal.core.dtos.requests.CrosswordPuzzleSubmitRequestDTO;
import com.lexipal.core.dtos.responses.ChallengeAttemptReadResponseDTO;
import com.lexipal.core.dtos.responses.CrosswordPuzzleReadResponseDTO;
import com.lexipal.core.dtos.responses.CrosswordPuzzleSubmitResponseDTO;
import com.lexipal.core.dtos.responses.XpUpdateResponseDTO;
import com.lexipal.core.entities.Challenge;
import com.lexipal.core.entities.ChallengeAttempt;
import com.lexipal.core.entities.ChallengeResult;
import com.lexipal.core.entities.ChallengeType;
import com.lexipal.core.entities.CrosswordPuzzle;
import com.lexipal.core.entities.CrosswordPuzzleStatus;
import com.lexipal.core.entities.CurrencyTransactionReason;
import com.lexipal.core.entities.User;
import com.lexipal.core.entities.Word;
import com.lexipal.core.entities.WordLookup;
import com.lexipal.core.exceptions.ResourceConflictException;
import com.lexipal.core.exceptions.ResourceNotFoundException;
import com.lexipal.core.repositories.ChallengeAttemptRepository;
import com.lexipal.core.repositories.ChallengeRepository;
import com.lexipal.core.repositories.CrosswordPuzzleRepository;
import com.lexipal.core.repositories.WordLookupRepository;
import com.lexipal.core.repositories.WordRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

@Service
@Transactional
public class CrosswordPuzzleService
{
    private static final Logger logger = LogManager.getLogger(CrosswordPuzzleService.class);
    private static final int WORD_COUNT = 5;
    private static final int PASS_XP = 10;
    private static final int FAIL_XP = 0;
    private static final int MAX_CANDIDATE_POOL = 80;
    private static final int MAX_GENERATION_ATTEMPTS = 120;
    private static final Pattern ALPHABETIC_LEMMA = Pattern.compile("[a-z]+");

    private final CrosswordPuzzleRepository crosswordPuzzleRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final WordLookupRepository wordLookupRepository;
    private final WordRepository wordRepository;
    private final UserServiceInterface userService;
    private final XpService xpService;
    private final CurrencyService currencyService;
    private final ObjectMapper objectMapper;

    public CrosswordPuzzleService(final CrosswordPuzzleRepository crosswordPuzzleRepository,
                                  final ChallengeRepository challengeRepository,
                                  final ChallengeAttemptRepository challengeAttemptRepository,
                                  final WordLookupRepository wordLookupRepository,
                                  final WordRepository wordRepository,
                                  final UserServiceInterface userService,
                                  final XpService xpService,
                                  final CurrencyService currencyService,
                                  final ObjectMapper objectMapper)
    {
        this.crosswordPuzzleRepository = crosswordPuzzleRepository;
        this.challengeRepository = challengeRepository;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.wordLookupRepository = wordLookupRepository;
        this.wordRepository = wordRepository;
        this.userService = userService;
        this.xpService = xpService;
        this.currencyService = currencyService;
        this.objectMapper = objectMapper;
    }

    public CrosswordPuzzleReadResponseDTO createPuzzle(String keycloakSub)
    {
        User user = userService.getRequiredUser(keycloakSub);
        Challenge crosswordChallenge = getCrosswordChallenge();
        Random random = ThreadLocalRandom.current();

        List<CandidateWord> candidates = new ArrayList<>();
        Set<String> seenLemmas = new LinkedHashSet<>();
        appendUserLookupCandidates(keycloakSub, candidates, seenLemmas, random);
        appendGlobalWordCandidates(candidates, seenLemmas, random);

        Optional<GeneratedCrossword> generated = tryGenerateCrossword(candidates, random, true);
        if (generated.isEmpty())
        {
            generated = tryGenerateCrossword(candidates, random, false);
        }

        GeneratedCrossword crossword = generated.orElseThrow(() ->
                new IllegalArgumentException("Not enough eligible words to generate a crossword puzzle."));

        CrosswordPuzzle puzzle = new CrosswordPuzzle();
        puzzle.setUser(user);
        puzzle.setChallenge(crosswordChallenge);
        puzzle.setStatus(CrosswordPuzzleStatus.ACTIVE);
        puzzle.setPublicPuzzle(crossword.publicPuzzle());
        puzzle.setSolution(crossword.solution());
        puzzle.setWordSources(crossword.wordSources());

        return toPuzzleReadResponse(crosswordPuzzleRepository.save(puzzle));
    }

    @Transactional(readOnly = true)
    public CrosswordPuzzleReadResponseDTO getPuzzle(Long puzzleId, String keycloakSub)
    {
        return toPuzzleReadResponse(getOwnedPuzzle(puzzleId, keycloakSub));
    }

    public CrosswordPuzzleSubmitResponseDTO submitPuzzle(Long puzzleId, CrosswordPuzzleSubmitRequestDTO dto, String keycloakSub)
    {
        CrosswordPuzzle puzzle = getOwnedPuzzle(puzzleId, keycloakSub);

        if (puzzle.getStatus() == CrosswordPuzzleStatus.SUBMITTED)
        {
            throw new ResourceConflictException("This crossword puzzle has already been submitted.");
        }

        Map<String, String> submittedAnswers = dto.getAnswers() == null ? Map.of() : dto.getAnswers();
        JsonNode solutionAnswers = puzzle.getSolution().get("answers");
        ArrayNode answerResults = objectMapper.createArrayNode();
        int correctCount = 0;
        int totalEntries = solutionAnswers.size();

        Iterator<Map.Entry<String, JsonNode>> fields = solutionAnswers.fields();
        while (fields.hasNext())
        {
            Map.Entry<String, JsonNode> field = fields.next();
            String entryId = field.getKey();
            String expectedAnswer = normalizeAnswer(field.getValue().asText());
            String submittedAnswer = normalizeAnswer(submittedAnswers.get(entryId));
            boolean correct = expectedAnswer.equals(submittedAnswer);

            if (correct)
            {
                correctCount++;
            }

            ObjectNode answerResult = objectMapper.createObjectNode();
            answerResult.put("entryId", entryId);
            answerResult.put("correct", correct);
            answerResults.add(answerResult);
        }

        ChallengeResult result = correctCount == WORD_COUNT ? ChallengeResult.PASS : ChallengeResult.FAIL;
        int xpAwarded = result == ChallengeResult.PASS ? PASS_XP : FAIL_XP;

        puzzle.setStatus(CrosswordPuzzleStatus.SUBMITTED);
        puzzle.setSubmittedAt(LocalDateTime.now());
        crosswordPuzzleRepository.save(puzzle);

        User user = puzzle.getUser();
        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setUser(user);
        attempt.setChallenge(puzzle.getChallenge());
        attempt.setCrosswordPuzzle(puzzle);
        attempt.setResult(result);
        attempt.setXpAwarded(xpAwarded);
        attempt.setUserAnswer(correctCount + "/" + totalEntries);

        ChallengeAttempt savedAttempt = challengeAttemptRepository.save(attempt);
        user.getChallengeAttempts().add(savedAttempt);
        puzzle.getChallenge().getAttempts().add(savedAttempt);

        XpUpdateResponseDTO xpUpdate = xpService.awardXp(user, xpAwarded);
        if (result == ChallengeResult.PASS)
        {
            currencyService.awardCoins(user,
                    CurrencyTransactionReason.CROSSWORD_PASSED,
                    "CROSSWORD_PUZZLE", puzzle.getId());
        }

        return new CrosswordPuzzleSubmitResponseDTO(
                toPuzzleReadResponse(puzzle),
                toChallengeAttemptReadResponse(savedAttempt, xpUpdate),
                result.name(),
                correctCount,
                totalEntries,
                answerResults
        );
    }

    private Challenge getCrosswordChallenge()
    {
        return challengeRepository.findByType(ChallengeType.CROSSWORD)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge", "type", ChallengeType.CROSSWORD.name()));
    }

    private CrosswordPuzzle getOwnedPuzzle(Long puzzleId, String keycloakSub)
    {
        return crosswordPuzzleRepository.findByIdAndUser_KeycloakSub(puzzleId, keycloakSub)
                .orElseThrow(() -> new ResourceNotFoundException("CrosswordPuzzle", "id", puzzleId));
    }

    private void appendUserLookupCandidates(String keycloakSub, List<CandidateWord> candidates, Set<String> seenLemmas,
                                            Random random)
    {
        List<CandidateWord> userLookupCandidates = new ArrayList<>();

        for (WordLookup wordLookup : wordLookupRepository.findByUser_KeycloakSubOrderByLastLookedUpAtDesc(keycloakSub))
        {
            appendCandidate(wordLookup.getWord(), wordLookup.getId(), WordSource.USER_LOOKUP, userLookupCandidates, seenLemmas);
        }

        Collections.shuffle(userLookupCandidates, random);
        appendCandidateTier(candidates, userLookupCandidates);
    }

    private void appendGlobalWordCandidates(List<CandidateWord> candidates, Set<String> seenLemmas, Random random)
    {
        List<CandidateWord> globalWordCandidates = new ArrayList<>();

        for (Word word : wordRepository.findAllByOrderByIdAsc())
        {
            appendCandidate(word, null, WordSource.GLOBAL_WORD, globalWordCandidates, seenLemmas);
        }

        Collections.shuffle(globalWordCandidates, random);
        appendCandidateTier(candidates, globalWordCandidates);
    }

    private void appendCandidateTier(List<CandidateWord> candidates, List<CandidateWord> candidateTier)
    {
        for (CandidateWord candidate : candidateTier)
        {
            if (candidates.size() >= MAX_CANDIDATE_POOL)
            {
                return;
            }

            candidates.add(candidate);
        }
    }

    private void appendCandidate(Word word, Long wordLookupId, WordSource source,
                                 List<CandidateWord> candidates, Set<String> seenLemmas)
    {
        if (word == null || candidates.size() >= MAX_CANDIDATE_POOL)
        {
            return;
        }

        String lemma = normalizeLemma(word.getLemma());
        if (!isEligibleLemma(lemma) || seenLemmas.contains(lemma))
        {
            return;
        }

        String clue = WordDefinitionExtractor.extractFirstDefinition(word);
        if (clue == null || clue.isBlank())
        {
            return;
        }

        seenLemmas.add(lemma);
        candidates.add(new CandidateWord(lemma, clue.trim(), word.getId(), wordLookupId, source));
    }

    private Optional<GeneratedCrossword> tryGenerateCrossword(List<CandidateWord> candidates, Random random,
                                                             boolean strictSourcePriority)
    {
        if (candidates.size() < WORD_COUNT)
        {
            return Optional.empty();
        }

        List<CandidateWord> candidatePool = candidates.stream()
                .limit(MAX_CANDIDATE_POOL)
                .toList();

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++)
        {
            List<CandidateWord> orderedCandidates = buildGenerationAttemptOrder(candidatePool, random, strictSourcePriority);

            GenerationState state = tryPlaceWords(orderedCandidates);
            if (state.placedWords().size() == WORD_COUNT)
            {
                return Optional.of(buildGeneratedCrossword(state));
            }
        }

        return Optional.empty();
    }

    private List<CandidateWord> buildGenerationAttemptOrder(List<CandidateWord> candidatePool, Random random,
                                                            boolean strictSourcePriority)
    {
        List<CandidateWord> userLookupCandidates = new ArrayList<>();
        List<CandidateWord> globalWordCandidates = new ArrayList<>();

        for (CandidateWord candidate : candidatePool)
        {
            switch (candidate.source())
            {
                case USER_LOOKUP -> userLookupCandidates.add(candidate);
                case GLOBAL_WORD -> globalWordCandidates.add(candidate);
            }
        }

        Collections.shuffle(userLookupCandidates, random);
        Collections.shuffle(globalWordCandidates, random);

        if (strictSourcePriority)
        {
            if (userLookupCandidates.size() >= WORD_COUNT)
            {
                return userLookupCandidates;
            }

            if (userLookupCandidates.size() + globalWordCandidates.size() >= WORD_COUNT)
            {
                List<CandidateWord> orderedCandidates = new ArrayList<>(userLookupCandidates.size() + globalWordCandidates.size());
                orderedCandidates.addAll(userLookupCandidates);
                orderedCandidates.addAll(globalWordCandidates);
                return orderedCandidates;
            }
        }

        List<CandidateWord> orderedCandidates = new ArrayList<>(candidatePool.size());
        orderedCandidates.addAll(userLookupCandidates);
        orderedCandidates.addAll(globalWordCandidates);

        return orderedCandidates;
    }

    private GenerationState tryPlaceWords(List<CandidateWord> candidates)
    {
        GenerationState state = new GenerationState();

        for (CandidateWord candidate : candidates)
        {
            if (state.placedWords().size() == WORD_COUNT)
            {
                break;
            }

            if (state.placedLemmas().contains(candidate.lemma()))
            {
                continue;
            }

            if (state.placedWords().isEmpty())
            {
                placeWord(candidate, 0, 0, Direction.ACROSS, state);
                continue;
            }

            Optional<Placement> placement = findBestPlacement(candidate, state);
            placement.ifPresent(value -> placeWord(candidate, value.row(), value.column(), value.direction(), state));
        }

        return state;
    }

    private Optional<Placement> findBestPlacement(CandidateWord candidate, GenerationState state)
    {
        Placement bestPlacement = null;
        int bestScore = Integer.MAX_VALUE;
        List<Point> occupiedPoints = new ArrayList<>(state.board().keySet());

        for (int wordIndex = 0; wordIndex < candidate.lemma().length(); wordIndex++)
        {
            char letter = candidate.lemma().charAt(wordIndex);

            for (Point occupiedPoint : occupiedPoints)
            {
                if (state.board().get(occupiedPoint) != letter)
                {
                    continue;
                }

                for (Direction direction : Direction.values())
                {
                    int row = occupiedPoint.row() - (direction == Direction.DOWN ? wordIndex : 0);
                    int column = occupiedPoint.column() - (direction == Direction.ACROSS ? wordIndex : 0);
                    Optional<PlacementStats> stats = canPlace(candidate.lemma(), row, column, direction, state);

                    if (stats.isPresent() && stats.get().score() < bestScore)
                    {
                        bestScore = stats.get().score();
                        bestPlacement = new Placement(row, column, direction);
                    }
                }
            }
        }

        return Optional.ofNullable(bestPlacement);
    }

    private Optional<PlacementStats> canPlace(String word, int row, int column, Direction direction, GenerationState state)
    {
        int intersections = 0;

        Point beforeStart = new Point(row - direction.rowOffset(), column - direction.columnOffset());
        Point afterEnd = new Point(
                row + direction.rowOffset() * word.length(),
                column + direction.columnOffset() * word.length()
        );

        if (state.board().containsKey(beforeStart) || state.board().containsKey(afterEnd))
        {
            return Optional.empty();
        }

        for (int index = 0; index < word.length(); index++)
        {
            Point point = new Point(
                    row + direction.rowOffset() * index,
                    column + direction.columnOffset() * index
            );
            Character existingLetter = state.board().get(point);

            if (existingLetter != null)
            {
                if (existingLetter != word.charAt(index)
                        || state.cellDirections().getOrDefault(point, EnumSet.noneOf(Direction.class)).contains(direction))
                {
                    return Optional.empty();
                }

                intersections++;
                continue;
            }

            Point firstPerpendicular = direction == Direction.ACROSS
                    ? new Point(point.row() - 1, point.column())
                    : new Point(point.row(), point.column() - 1);
            Point secondPerpendicular = direction == Direction.ACROSS
                    ? new Point(point.row() + 1, point.column())
                    : new Point(point.row(), point.column() + 1);

            if (state.board().containsKey(firstPerpendicular) || state.board().containsKey(secondPerpendicular))
            {
                return Optional.empty();
            }
        }

        if (intersections == 0)
        {
            return Optional.empty();
        }

        int area = calculateAreaWithWord(word, row, column, direction, state);
        return Optional.of(new PlacementStats(intersections, area * 100 - intersections * 25));
    }

    private void placeWord(CandidateWord candidate, int row, int column, Direction direction, GenerationState state)
    {
        PlacedWord placedWord = new PlacedWord(candidate, row, column, direction);
        state.placedWords().add(placedWord);
        state.placedLemmas().add(candidate.lemma());

        for (int index = 0; index < candidate.lemma().length(); index++)
        {
            Point point = new Point(
                    row + direction.rowOffset() * index,
                    column + direction.columnOffset() * index
            );

            state.board().put(point, candidate.lemma().charAt(index));
            state.cellDirections()
                    .computeIfAbsent(point, ignored -> EnumSet.noneOf(Direction.class))
                    .add(direction);
        }
    }

    private GeneratedCrossword buildGeneratedCrossword(GenerationState state)
    {
        Bounds bounds = calculateBounds(state.board().keySet());
        List<PlacedWord> sortedWords = state.placedWords().stream()
                .sorted(Comparator
                        .comparingInt((PlacedWord placedWord) -> normalizeRow(placedWord.row(), bounds))
                        .thenComparingInt(placedWord -> normalizeColumn(placedWord.column(), bounds))
                        .thenComparing(placedWord -> placedWord.direction().name()))
                .toList();

        Map<Point, Integer> numberByStart = new LinkedHashMap<>();
        int nextNumber = 1;
        for (PlacedWord placedWord : sortedWords)
        {
            Point start = normalizedStart(placedWord, bounds);
            if (!numberByStart.containsKey(start))
            {
                numberByStart.put(start, nextNumber++);
            }
        }

        ArrayNode entries = objectMapper.createArrayNode();
        ObjectNode answers = objectMapper.createObjectNode();
        ArrayNode wordSources = objectMapper.createArrayNode();
        Map<Point, List<String>> entryIdsByCell = new LinkedHashMap<>();

        for (PlacedWord placedWord : sortedWords)
        {
            Point start = normalizedStart(placedWord, bounds);
            int number = numberByStart.get(start);
            String entryId = number + "-" + placedWord.direction().name().toLowerCase(Locale.ROOT);

            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("id", entryId);
            entry.put("number", number);
            entry.put("direction", placedWord.direction().name());
            entry.put("row", start.row());
            entry.put("column", start.column());
            entry.put("length", placedWord.candidate().lemma().length());
            entry.put("clue", placedWord.candidate().clue());
            entries.add(entry);

            answers.put(entryId, placedWord.candidate().lemma());

            ObjectNode source = objectMapper.createObjectNode();
            source.put("entryId", entryId);
            source.put("source", placedWord.candidate().source().name());
            putNullable(source, "wordId", placedWord.candidate().wordId());
            putNullable(source, "wordLookupId", placedWord.candidate().wordLookupId());
            wordSources.add(source);

            for (int index = 0; index < placedWord.candidate().lemma().length(); index++)
            {
                Point point = new Point(
                        normalizeRow(placedWord.row() + placedWord.direction().rowOffset() * index, bounds),
                        normalizeColumn(placedWord.column() + placedWord.direction().columnOffset() * index, bounds)
                );
                entryIdsByCell.computeIfAbsent(point, ignored -> new ArrayList<>()).add(entryId);
            }
        }

        ArrayNode cells = objectMapper.createArrayNode();
        entryIdsByCell.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator
                        .comparingInt(Point::row)
                        .thenComparingInt(Point::column)))
                .forEach(entry -> {
                    ObjectNode cell = objectMapper.createObjectNode();
                    cell.put("row", entry.getKey().row());
                    cell.put("column", entry.getKey().column());

                    Integer number = numberByStart.get(entry.getKey());
                    if (number != null)
                    {
                        cell.put("number", number);
                    }

                    ArrayNode entryIds = objectMapper.createArrayNode();
                    entry.getValue().forEach(entryIds::add);
                    cell.set("entryIds", entryIds);
                    cells.add(cell);
                });

        ObjectNode grid = objectMapper.createObjectNode();
        grid.put("rows", bounds.maxRow() - bounds.minRow() + 1);
        grid.put("columns", bounds.maxColumn() - bounds.minColumn() + 1);
        grid.set("cells", cells);

        ObjectNode publicPuzzle = objectMapper.createObjectNode();
        publicPuzzle.put("wordCount", WORD_COUNT);
        publicPuzzle.set("grid", grid);
        publicPuzzle.set("entries", entries);

        ObjectNode solution = objectMapper.createObjectNode();
        solution.set("answers", answers);

        return new GeneratedCrossword(publicPuzzle, solution, wordSources);
    }

    private int calculateAreaWithWord(String word, int row, int column, Direction direction, GenerationState state)
    {
        Set<Point> points = new HashSet<>(state.board().keySet());

        for (int index = 0; index < word.length(); index++)
        {
            points.add(new Point(
                    row + direction.rowOffset() * index,
                    column + direction.columnOffset() * index
            ));
        }

        Bounds bounds = calculateBounds(points);
        return (bounds.maxRow() - bounds.minRow() + 1) * (bounds.maxColumn() - bounds.minColumn() + 1);
    }

    private Bounds calculateBounds(Set<Point> points)
    {
        int minRow = points.stream().mapToInt(Point::row).min().orElse(0);
        int maxRow = points.stream().mapToInt(Point::row).max().orElse(0);
        int minColumn = points.stream().mapToInt(Point::column).min().orElse(0);
        int maxColumn = points.stream().mapToInt(Point::column).max().orElse(0);

        return new Bounds(minRow, maxRow, minColumn, maxColumn);
    }

    private CrosswordPuzzleReadResponseDTO toPuzzleReadResponse(CrosswordPuzzle puzzle)
    {
        return new CrosswordPuzzleReadResponseDTO(
                puzzle.getId(),
                puzzle.getChallenge().getId(),
                puzzle.getStatus().name(),
                puzzle.getCreatedAt(),
                puzzle.getSubmittedAt(),
                puzzle.getPublicPuzzle()
        );
    }

    private ChallengeAttemptReadResponseDTO toChallengeAttemptReadResponse(ChallengeAttempt attempt,
                                                                           XpUpdateResponseDTO xpUpdate)
    {
        return new ChallengeAttemptReadResponseDTO(
                attempt.getId(),
                attempt.getUserAnswer(),
                attempt.getResult().name(),
                attempt.getXpAwarded(),
                attempt.getAttemptedAt(),
                attempt.getChallenge().getId(),
                attempt.getChallenge().getType().name(),
                null,
                null,
                null,
                null,
                attempt.getCrosswordPuzzle() != null ? attempt.getCrosswordPuzzle().getId() : null,
                xpUpdate
        );
    }

    private boolean isEligibleLemma(String lemma)
    {
        return lemma.length() >= 3 && lemma.length() <= 12 && ALPHABETIC_LEMMA.matcher(lemma).matches();
    }

    private String normalizeLemma(String lemma)
    {
        return lemma == null ? "" : lemma.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAnswer(String answer)
    {
        if (answer == null)
        {
            return "";
        }

        return answer.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private Point normalizedStart(PlacedWord placedWord, Bounds bounds)
    {
        return new Point(normalizeRow(placedWord.row(), bounds), normalizeColumn(placedWord.column(), bounds));
    }

    private int normalizeRow(int row, Bounds bounds)
    {
        return row - bounds.minRow();
    }

    private int normalizeColumn(int column, Bounds bounds)
    {
        return column - bounds.minColumn();
    }

    private void putNullable(ObjectNode objectNode, String fieldName, Long value)
    {
        if (value == null)
        {
            objectNode.putNull(fieldName);
            return;
        }

        objectNode.put(fieldName, value);
    }

    private enum Direction
    {
        ACROSS(0, 1),
        DOWN(1, 0);

        private final int rowOffset;
        private final int columnOffset;

        Direction(int rowOffset, int columnOffset)
        {
            this.rowOffset = rowOffset;
            this.columnOffset = columnOffset;
        }

        int rowOffset()
        {
            return rowOffset;
        }

        int columnOffset()
        {
            return columnOffset;
        }
    }

    private enum WordSource
    {
        USER_LOOKUP,
        GLOBAL_WORD
    }

    private record CandidateWord(String lemma, String clue, Long wordId, Long wordLookupId, WordSource source)
    {
    }

    private record PlacedWord(CandidateWord candidate, int row, int column, Direction direction)
    {
    }

    private record Placement(int row, int column, Direction direction)
    {
    }

    private record PlacementStats(int intersections, int score)
    {
    }

    private record Point(int row, int column)
    {
    }

    private record Bounds(int minRow, int maxRow, int minColumn, int maxColumn)
    {
    }

    private record GeneratedCrossword(JsonNode publicPuzzle, JsonNode solution, JsonNode wordSources)
    {
    }

    private static final class GenerationState
    {
        private final Map<Point, Character> board = new LinkedHashMap<>();
        private final Map<Point, EnumSet<Direction>> cellDirections = new LinkedHashMap<>();
        private final List<PlacedWord> placedWords = new ArrayList<>();
        private final Set<String> placedLemmas = new HashSet<>();

        Map<Point, Character> board()
        {
            return board;
        }

        Map<Point, EnumSet<Direction>> cellDirections()
        {
            return cellDirections;
        }

        List<PlacedWord> placedWords()
        {
            return placedWords;
        }

        Set<String> placedLemmas()
        {
            return placedLemmas;
        }
    }
}
