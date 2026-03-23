package com.lexipal.core.controllers;


import com.lexipal.core.dtos.CaptureRequestDTO;
import com.lexipal.core.dtos.CaptureResponseDTO;
import com.lexipal.core.services.CapturedWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lexipal/api/v1/capturedWords")
public class CapturedWordController
{
    private final CapturedWordService capturedWordService;

    @Autowired
    public CapturedWordController(final CapturedWordService capturedWordService)
    {
        this.capturedWordService = capturedWordService;
    }

    @PostMapping
    public ResponseEntity<CaptureResponseDTO> searchAndCapture(@RequestBody CaptureRequestDTO requestDTO)
    {
        CaptureResponseDTO response = capturedWordService.performSearchAndCapture(requestDTO);

        return ResponseEntity.ok(response);
    }
}
