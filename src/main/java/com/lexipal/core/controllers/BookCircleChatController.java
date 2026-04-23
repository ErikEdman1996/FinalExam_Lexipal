package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.CircleChatHistoryResponseDTO;
import com.lexipal.core.services.BookCircleChatServiceInterface;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/lexipal/api/v1/book-circles/{circleId}/chat/messages")
public class BookCircleChatController
{
    private final BookCircleChatServiceInterface bookCircleChatService;

    @Autowired
    public BookCircleChatController(final BookCircleChatServiceInterface bookCircleChatService)
    {
        this.bookCircleChatService = bookCircleChatService;
    }

    @GetMapping
    public ResponseEntity<CircleChatHistoryResponseDTO> getMessages(
            @PathVariable Long circleId,
            @RequestParam(required = false) Long beforeMessageId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) Integer limit,
            @AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(
                bookCircleChatService.getMessages(circleId, beforeMessageId, limit, jwt.getSubject())
        );
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long circleId,
                                           @PathVariable Long messageId,
                                           @AuthenticationPrincipal Jwt jwt)
    {
        bookCircleChatService.deleteMessage(circleId, messageId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
