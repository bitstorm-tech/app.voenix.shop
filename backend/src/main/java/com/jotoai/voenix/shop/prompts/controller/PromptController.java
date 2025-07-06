package com.jotoai.voenix.shop.prompts.controller;

import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest;
import com.jotoai.voenix.shop.prompts.dto.PromptDto;
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest;
import com.jotoai.voenix.shop.prompts.service.PromptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {
    
    private final PromptService promptService;
    
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }
    
    @GetMapping
    public ResponseEntity<List<PromptDto>> getAllPrompts() {
        return ResponseEntity.ok(promptService.getAllPrompts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PromptDto> getPromptById(@PathVariable Long id) {
        return ResponseEntity.ok(promptService.getPromptById(id));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PromptDto>> searchPrompts(@RequestParam String title) {
        return ResponseEntity.ok(promptService.searchPromptsByTitle(title));
    }
    
    @PostMapping
    public ResponseEntity<PromptDto> createPrompt(@Valid @RequestBody CreatePromptRequest request) {
        PromptDto createdPrompt = promptService.createPrompt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPrompt);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PromptDto> updatePrompt(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromptRequest request) {
        return ResponseEntity.ok(promptService.updatePrompt(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }
}