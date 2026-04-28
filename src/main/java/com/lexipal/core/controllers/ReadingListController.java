package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.ReadingListCreateRequestDTO;
import com.lexipal.core.dtos.requests.ReadingListUpdateRequestDTO;
import com.lexipal.core.dtos.responses.ReadingListDetailResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSubscriptionResponseDTO;
import com.lexipal.core.dtos.responses.ReadingListSummaryResponseDTO;
import com.lexipal.core.services.ReadingListServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/reading-lists")
public class ReadingListController
{
    private final ReadingListServiceInterface readingListService;

    @Autowired
    public ReadingListController(final ReadingListServiceInterface readingListService)
    {
        this.readingListService = readingListService;
    }

    @PostMapping
    public ResponseEntity<ReadingListDetailResponseDTO> createList(
            @Valid @RequestBody ReadingListCreateRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(readingListService.createList(request, keycloakSub));
    }

    @GetMapping
    public ResponseEntity<List<ReadingListSummaryResponseDTO>> browseLists(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(readingListService.browseLists(q, minStars, page, size, sort, keycloakSub));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReadingListSummaryResponseDTO>> getMyLists(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(readingListService.getMyLists(keycloakSub));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<ReadingListSubscriptionResponseDTO>> getMySubscriptions(
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(readingListService.getMySubscriptions(keycloakSub));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingListDetailResponseDTO> getList(@PathVariable Long id,
                                                                @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(readingListService.getList(id, keycloakSub));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReadingListDetailResponseDTO> updateList(
            @PathVariable Long id,
            @Valid @RequestBody ReadingListUpdateRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(readingListService.updateList(id, request, keycloakSub));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteList(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        readingListService.deleteList(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        readingListService.subscribe(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/subscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        readingListService.unsubscribe(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/star")
    public ResponseEntity<?> star(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        readingListService.starList(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/star")
    public ResponseEntity<?> unstar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        readingListService.unstarList(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }
}
