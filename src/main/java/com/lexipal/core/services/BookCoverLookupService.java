package com.lexipal.core.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.lexipal.core.models.BookCoverMatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookCoverLookupService
{
    public static final String SOURCE_OPEN_LIBRARY = "OPEN_LIBRARY";
    public static final String SOURCE_GOOGLE_BOOKS = "GOOGLE_BOOKS";

    private static final Logger logger = LogManager.getLogger(BookCoverLookupService.class);
    private static final String OPEN_LIBRARY_COVER_URL = "https://covers.openlibrary.org/b/id/%s-L.jpg";
    private static final int CONFIDENT_MATCH_THRESHOLD = 70;
    private static final int MAX_CANDIDATES = 5;

    private final RestClient openLibraryClient;
    private final RestClient googleBooksClient;
    private final boolean coverLookupEnabled;
    private final boolean googleBooksEnabled;
    private final String googleBooksApiKey;

    @Autowired
    public BookCoverLookupService(final RestClient.Builder restClientBuilder,
                                  @Value("${lexipal.book-covers.enabled:true}") boolean coverLookupEnabled,
                                  @Value("${lexipal.book-covers.google.enabled:false}") boolean googleBooksEnabled,
                                  @Value("${lexipal.book-covers.google.api-key:}") String googleBooksApiKey)
    {
        this(createClient(restClientBuilder, "https://openlibrary.org"),
                createClient(restClientBuilder, "https://www.googleapis.com/books/v1"),
                coverLookupEnabled,
                googleBooksEnabled,
                googleBooksApiKey);
    }

    BookCoverLookupService(final RestClient openLibraryClient,
                           final RestClient googleBooksClient,
                           boolean coverLookupEnabled,
                           boolean googleBooksEnabled,
                           String googleBooksApiKey)
    {
        this.openLibraryClient = openLibraryClient;
        this.googleBooksClient = googleBooksClient;
        this.coverLookupEnabled = coverLookupEnabled;
        this.googleBooksEnabled = googleBooksEnabled;
        this.googleBooksApiKey = googleBooksApiKey == null ? "" : googleBooksApiKey.trim();
    }

    private static RestClient createClient(RestClient.Builder restClientBuilder, String baseUrl)
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(8));

        return restClientBuilder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public Optional<BookCoverMatch> findBestCover(String title, String author)
    {
        return findCoverCandidates(title, author).stream()
                .filter(candidate -> candidate.confidence() >= CONFIDENT_MATCH_THRESHOLD)
                .max(Comparator.comparingInt(BookCoverMatch::confidence));
    }

    public List<BookCoverMatch> findCoverCandidates(String title, String author)
    {
        String cleanTitle = clean(title);
        String cleanAuthor = clean(author);

        if (!coverLookupEnabled || cleanTitle.isBlank() || cleanAuthor.isBlank())
        {
            return List.of();
        }

        List<BookCoverMatch> candidates = new ArrayList<>(findOpenLibraryCandidates(cleanTitle, cleanAuthor));

        if (googleBooksEnabled && candidates.stream().noneMatch(candidate -> candidate.confidence() >= CONFIDENT_MATCH_THRESHOLD))
        {
            candidates.addAll(findGoogleBooksCandidates(cleanTitle, cleanAuthor));
        }

        return candidates.stream()
                .filter(candidate -> candidate.coverImageUrl() != null && !candidate.coverImageUrl().isBlank())
                .filter(candidate -> candidate.confidence() >= CONFIDENT_MATCH_THRESHOLD)
                .sorted(Comparator.comparingInt(BookCoverMatch::confidence).reversed())
                .limit(MAX_CANDIDATES)
                .toList();
    }

    private List<BookCoverMatch> findOpenLibraryCandidates(String title, String author)
    {
        try
        {
            JsonNode response = openLibraryClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("title", title)
                            .queryParam("author", author)
                            .queryParam("fields", "key,title,author_name,cover_i")
                            .queryParam("limit", MAX_CANDIDATES)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode docs = response == null ? null : response.get("docs");
            if (docs == null || !docs.isArray())
            {
                return List.of();
            }

            List<BookCoverMatch> candidates = new ArrayList<>();
            for (JsonNode doc : docs)
            {
                JsonNode cover = doc.get("cover_i");
                if (cover == null || !cover.canConvertToLong())
                {
                    continue;
                }

                String matchedTitle = textValue(doc.get("title"));
                String matchedAuthor = firstTextValue(doc.get("author_name"));
                int confidence = scoreMatch(title, author, matchedTitle, matchedAuthor);

                candidates.add(new BookCoverMatch(
                        String.format(OPEN_LIBRARY_COVER_URL, cover.asLong()),
                        SOURCE_OPEN_LIBRARY,
                        textValue(doc.get("key")),
                        matchedTitle,
                        matchedAuthor,
                        confidence
                ));
            }

            return candidates;
        }
        catch (RestClientException | IllegalArgumentException ex)
        {
            logger.warn("Open Library cover lookup failed for '{}' by '{}'", title, author, ex);
            return List.of();
        }
    }

    private List<BookCoverMatch> findGoogleBooksCandidates(String title, String author)
    {
        try
        {
            JsonNode response = googleBooksClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/volumes")
                                .queryParam("q", "intitle:" + title + " inauthor:" + author)
                                .queryParam("maxResults", MAX_CANDIDATES)
                                .queryParam("printType", "books");

                        if (!googleBooksApiKey.isBlank())
                        {
                            builder.queryParam("key", googleBooksApiKey);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode items = response == null ? null : response.get("items");
            if (items == null || !items.isArray())
            {
                return List.of();
            }

            List<BookCoverMatch> candidates = new ArrayList<>();
            for (JsonNode item : items)
            {
                JsonNode volumeInfo = item.get("volumeInfo");
                JsonNode imageLinks = volumeInfo == null ? null : volumeInfo.get("imageLinks");
                String coverUrl = bestGoogleCoverUrl(imageLinks);

                if (coverUrl == null)
                {
                    continue;
                }

                String matchedTitle = volumeInfo == null ? null : textValue(volumeInfo.get("title"));
                String matchedAuthor = volumeInfo == null ? null : firstTextValue(volumeInfo.get("authors"));
                int confidence = scoreMatch(title, author, matchedTitle, matchedAuthor);

                candidates.add(new BookCoverMatch(
                        ensureHttps(coverUrl),
                        SOURCE_GOOGLE_BOOKS,
                        textValue(item.get("id")),
                        matchedTitle,
                        matchedAuthor,
                        confidence
                ));
            }

            return candidates;
        }
        catch (RestClientException | IllegalArgumentException ex)
        {
            logger.warn("Google Books cover lookup failed for '{}' by '{}'", title, author, ex);
            return List.of();
        }
    }

    private String bestGoogleCoverUrl(JsonNode imageLinks)
    {
        if (imageLinks == null || imageLinks.isMissingNode())
        {
            return null;
        }

        for (String key : List.of("extraLarge", "large", "medium", "small", "thumbnail", "smallThumbnail"))
        {
            String value = textValue(imageLinks.get(key));
            if (value != null && !value.isBlank())
            {
                return value;
            }
        }

        return null;
    }

    int scoreMatch(String requestedTitle, String requestedAuthor, String matchedTitle, String matchedAuthor)
    {
        String normalizedRequestedTitle = normalizeForMatch(requestedTitle);
        String normalizedMatchedTitle = normalizeForMatch(matchedTitle);
        String normalizedRequestedAuthor = normalizeForMatch(requestedAuthor);
        String normalizedMatchedAuthor = normalizeForMatch(matchedAuthor);

        if (normalizedRequestedTitle.isBlank() || normalizedMatchedTitle.isBlank()
                || normalizedRequestedAuthor.isBlank() || normalizedMatchedAuthor.isBlank())
        {
            return 0;
        }

        int titleScore = normalizedRequestedTitle.equals(normalizedMatchedTitle)
                ? 60
                : Math.round(tokenOverlap(normalizedRequestedTitle, normalizedMatchedTitle) * 60);

        int authorScore = normalizedRequestedAuthor.equals(normalizedMatchedAuthor)
                ? 40
                : Math.round(tokenOverlap(normalizedRequestedAuthor, normalizedMatchedAuthor) * 40);

        return Math.min(100, titleScore + authorScore);
    }

    private float tokenOverlap(String left, String right)
    {
        Set<String> leftTokens = tokenize(left);
        Set<String> rightTokens = tokenize(right);

        if (leftTokens.isEmpty() || rightTokens.isEmpty())
        {
            return 0;
        }

        long matchingTokens = leftTokens.stream()
                .filter(rightTokens::contains)
                .count();

        return (float) matchingTokens / Math.max(leftTokens.size(), rightTokens.size());
    }

    private Set<String> tokenize(String value)
    {
        Set<String> tokens = new LinkedHashSet<>();

        for (String token : value.split(" "))
        {
            if (!token.isBlank())
            {
                tokens.add(token);
            }
        }

        return tokens;
    }

    private String normalizeForMatch(String value)
    {
        String cleaned = clean(value);
        String withoutAccents = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return withoutAccents
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String clean(String value)
    {
        return value == null ? "" : value.trim();
    }

    private String firstTextValue(JsonNode node)
    {
        if (node == null || !node.isArray() || node.isEmpty())
        {
            return null;
        }

        return textValue(node.get(0));
    }

    private String textValue(JsonNode node)
    {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private String ensureHttps(String url)
    {
        return url == null ? null : url.replaceFirst("^http://", "https://");
    }
}
