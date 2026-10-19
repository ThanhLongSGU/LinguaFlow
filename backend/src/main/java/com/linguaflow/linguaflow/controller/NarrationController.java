package com.linguaflow.linguaflow.controller;

import com.linguaflow.linguaflow.dto.NarrationDTO;
import com.linguaflow.linguaflow.entity.Narration;
import com.linguaflow.linguaflow.service.NarrationMapper;
import com.linguaflow.linguaflow.service.NarrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/narrations")
@RequiredArgsConstructor
public class NarrationController {

    private final NarrationService narrationService;
    private final NarrationMapper narrationMapper;

    @PostMapping
    public ResponseEntity<NarrationDTO> createNarration(@RequestBody NarrationDTO narrationDTO) {
        Narration narration = narrationService.createNarration(
                narrationDTO.getTitle(),
                narrationDTO.getOriginalContent(),
                narrationDTO.getSiteId());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(narrationMapper.toDto(narration));
    }
}