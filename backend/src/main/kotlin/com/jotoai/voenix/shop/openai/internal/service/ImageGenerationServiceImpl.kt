package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Simplified implementation focused solely on OpenAI API interactions.
 * Storage and orchestration concerns are handled by the ImageGenerationOrchestrationService.
 */
@Service
class OpenAIImageGenerationServiceImpl(
    private val openAIImageFacade: OpenAIImageFacade,
) : OpenAIImageGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun generateImages(
        processedImageFile: MultipartFile,
        request: PublicImageGenerationRequest,
    ): List<ByteArray> {
        logger.info { "Generating images with OpenAI for prompt ID: ${request.promptId}" }
        val openAIRequest = createOpenAIRequest(request)
        val imageEditResponse = openAIImageFacade.editImageBytes(processedImageFile, openAIRequest)
        logger.info { "Successfully generated ${imageEditResponse.imageBytes.size} images with OpenAI" }
        return imageEditResponse.imageBytes
    }

    override fun generateImages(
        processedImageFile: MultipartFile,
        promptId: Long,
    ): List<ByteArray> {
        logger.info { "Generating images with OpenAI for prompt ID: $promptId" }
        val openAIRequest = createOpenAIRequest(promptId)
        val imageEditResponse = openAIImageFacade.editImageBytes(processedImageFile, openAIRequest)
        logger.info { "Successfully generated ${imageEditResponse.imageBytes.size} images with OpenAI" }
        return imageEditResponse.imageBytes
    }



    private fun createOpenAIRequest(request: PublicImageGenerationRequest): CreateImageEditRequest =
        CreateImageEditRequest(
            promptId = request.promptId,
            background = request.background,
            quality = request.quality,
            size = request.size,
            n = request.n,
        )

    private fun createOpenAIRequest(promptId: Long): CreateImageEditRequest =
        createOpenAIRequest(PublicImageGenerationRequest(promptId = promptId, n = 4))
}
