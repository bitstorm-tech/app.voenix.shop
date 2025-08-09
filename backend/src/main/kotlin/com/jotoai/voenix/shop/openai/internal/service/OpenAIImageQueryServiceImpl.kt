package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.openai.api.OpenAIImageQueryService
import org.springframework.stereotype.Service

/**
 * Implementation of OpenAI image query service.
 * Currently, the OpenAI module doesn't have persistent data to query,
 * but this service is created for consistency with other modules and future extensibility.
 */
@Service
class OpenAIImageQueryServiceImpl : OpenAIImageQueryService {
    // Currently no implementation needed as OpenAI module doesn't persist query data
    // If image generation history, prompts, or metadata need to be queried in the future,
    // the implementation would be added here
}
