package com.lexipal.core.controllers;

import com.lexipal.core.dtos.requests.BookCircleCreateRequestDTO;
import com.lexipal.core.dtos.requests.BookCircleJoinRequestDTO;
import com.lexipal.core.dtos.responses.BookCircleCreateResponseDTO;
import com.lexipal.core.dtos.responses.BookCircleReadResponseDTO;
import com.lexipal.core.dtos.responses.CircleMemberDetailResponseDTO;
import com.lexipal.core.services.BookCircleServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lexipal/api/v1/book-circles")
public class BookCircleController
{
    private final BookCircleServiceInterface bookCircleService;

    @Autowired
    public BookCircleController(final BookCircleServiceInterface bookCircleService)
    {
        this.bookCircleService = bookCircleService;
    }

    @PostMapping
    public ResponseEntity<BookCircleCreateResponseDTO> createCircle(
            @Valid @RequestBody BookCircleCreateRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookCircleService.createCircle(request, keycloakSub));
    }

    @PostMapping("/join")
    public ResponseEntity<BookCircleReadResponseDTO> joinCircle(
            @Valid @RequestBody BookCircleJoinRequestDTO request,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(
                bookCircleService.joinCircle(request.getInviteCode().toUpperCase(), keycloakSub));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookCircleReadResponseDTO> getCircle(@PathVariable Long id,
                                                               @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(bookCircleService.getCircle(id, keycloakSub));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<BookCircleReadResponseDTO>> getMyCircles(@AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(bookCircleService.getMyCircles(keycloakSub));
    }

    @GetMapping("/{id}/members/{userId}/details")
    public ResponseEntity<CircleMemberDetailResponseDTO> getMemberDetails(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        return ResponseEntity.ok(bookCircleService.getMemberDetails(id, userId, keycloakSub));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId,
                                          @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        bookCircleService.removeMember(id, userId, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<?> leaveCircle(@PathVariable Long id,
                                         @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        bookCircleService.leaveCircle(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> closeCircle(@PathVariable Long id,
                                         @AuthenticationPrincipal Jwt jwt)
    {
        String keycloakSub = jwt.getSubject();
        bookCircleService.closeCircle(id, keycloakSub);
        return ResponseEntity.noContent().build();
    }
}
