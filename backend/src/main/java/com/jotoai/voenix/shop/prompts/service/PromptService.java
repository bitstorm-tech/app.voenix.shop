package com.jotoai.voenix.shop.prompts.service;

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException;
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest;
import com.jotoai.voenix.shop.prompts.dto.PromptDto;
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest;
import com.jotoai.voenix.shop.prompts.entity.Prompt;
import com.jotoai.voenix.shop.prompts.repository.PromptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PromptService {
    
    private final PromptRepository promptRepository;
    
    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }
    
    public List<PromptDto> getAllPrompts() {
        return promptRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public PromptDto getPromptById(Long id) {
        return promptRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", id));
    }
    
    public List<PromptDto> searchPromptsByTitle(String title) {
        return promptRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public PromptDto createPrompt(CreatePromptRequest request) {
        var prompt = Prompt.builder()
                .title(request.title())
                .content(request.content())
                .build();
        
        var savedPrompt = promptRepository.save(prompt);
        return toDto(savedPrompt);
    }
    
    @Transactional
    public PromptDto updatePrompt(Long id, UpdatePromptRequest request) {
        var prompt = promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", id));
        
        if (request.title() != null) {
            prompt.setTitle(request.title());
        }
        if (request.content() != null) {
            prompt.setContent(request.content());
        }
        
        var updatedPrompt = promptRepository.save(prompt);
        return toDto(updatedPrompt);
    }
    
    @Transactional
    public void deletePrompt(Long id) {
        if (!promptRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prompt", "id", id);
        }
        promptRepository.deleteById(id);
    }
    
    private PromptDto toDto(Prompt prompt) {
        return new PromptDto(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getContent(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }
}