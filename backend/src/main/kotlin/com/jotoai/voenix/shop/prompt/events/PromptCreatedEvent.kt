package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new prompt is created.
 */
@Externalized
data class PromptCreatedEvent(
    val prompt: PromptDto,
)
