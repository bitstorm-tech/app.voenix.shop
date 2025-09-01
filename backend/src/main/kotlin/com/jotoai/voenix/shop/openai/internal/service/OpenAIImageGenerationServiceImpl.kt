package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.openai.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.ImageGenerationRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Simplified implementation focused solely on OpenAI API interactions.
 * Storage and orchestration concerns are handled by the ImageGenerationOrchestrationService.
 */
@Service
class OpenAIImageGenerationServiceImpl(
    private val openAIImageFacade: OpenAIImageFacadeImpl,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun generateImages(
        imageBytes: ByteArray,
        promptId: Long,
    ): List<ByteArray> {
        logger.info { "Generating images with OpenAI for prompt ID: $promptId" }
        val openAIRequest = createOpenAIRequest(promptId)
        val imageEditResponse = openAIImageFacade.editImageBytes(imageBytes, openAIRequest)
        logger.info { "Successfully generated ${imageEditResponse.imageBytes.size} images with OpenAI" }
        return imageEditResponse.imageBytes
    }

    private fun createOpenAIRequest(request: ImageGenerationRequest): CreateImageEditRequest =
        CreateImageEditRequest(
            promptId = request.promptId,
            background = request.background,
            quality = request.quality,
            size = request.size,
            n = request.n,
        )

    private fun createOpenAIRequest(promptId: Long): CreateImageEditRequest =
        createOpenAIRequest(ImageGenerationRequest(promptId = promptId, n = 4))
}
