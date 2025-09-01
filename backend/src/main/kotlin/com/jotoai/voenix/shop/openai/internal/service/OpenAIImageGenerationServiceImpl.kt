package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.openai.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.ImageGenerationRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Simplified implementation focused solely on OpenAI API interactions.
 * Storage and orchestration concerns are handled by the ImageGenerationOrchestrationService.
 */
@Service
class OpenAIImageGenerationServiceImpl(
    private val openAIImageService: OpenAIImageService,
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
        
        // Create a simple MultipartFile wrapper for the byte array
        val multipartFile = object : MultipartFile {
            override fun getName(): String = "file"
            override fun getOriginalFilename(): String = "image.png"
            override fun getContentType(): String = "image/png"
            override fun isEmpty(): Boolean = imageBytes.isEmpty()
            override fun getSize(): Long = imageBytes.size.toLong()
            override fun getBytes(): ByteArray = imageBytes
            override fun getInputStream() = imageBytes.inputStream()
            override fun transferTo(dest: java.io.File) { dest.writeBytes(imageBytes) }
        }
        
        val imageEditResponse = openAIImageService.generateImages(multipartFile, openAIRequest)
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
