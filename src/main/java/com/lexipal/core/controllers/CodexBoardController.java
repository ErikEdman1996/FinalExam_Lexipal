package com.lexipal.core.controllers;

import com.lexipal.core.dtos.responses.CodexBoardResponseDTO;
import com.lexipal.core.services.CodexBoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/lexipal/api/v1/codex-board", "/lexipal/api/v1/codexBoard"})
public class CodexBoardController
{
    private final CodexBoardService codexBoardService;

    public CodexBoardController(final CodexBoardService codexBoardService)
    {
        this.codexBoardService = codexBoardService;
    }

    @GetMapping
    public ResponseEntity<CodexBoardResponseDTO> getBoard(@AuthenticationPrincipal Jwt jwt)
    {
        return ResponseEntity.ok(codexBoardService.getBoardForUser(jwt.getSubject()));
    }
}
