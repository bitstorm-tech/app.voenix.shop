package com.jotoai.voenix.shop.prompts.dto;

import java.time.LocalDateTime;

public record PromptDto(
    Long id,
    String title,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}