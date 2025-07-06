package com.jotoai.voenix.shop.prompts.dto;

import jakarta.validation.constraints.Size;

public record UpdatePromptRequest(
    @Size(max = 500, message = "Title must not exceed 500 characters")
    String title,
    
    String content
) {}