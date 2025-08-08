package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a prompt is updated.
 */
@Externalized
data class PromptUpdatedEvent(
    val oldPrompt: PromptDto,
    val newPrompt: PromptDto,
)
