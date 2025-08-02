package com.jotoai.voenix.shop.domain.openai.service

import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Test mode implementation of image generation strategy.
 * Returns the original image N times instead of calling external APIs.
 * Used for development and testing purposes.
 */
@Service
@ConditionalOnProperty(name = ["app.test-mode"], havingValue = "true")
class TestModeImageGenerationStrategy(
    private val promptService: PromptService,
) : ImageGenerationStrategy {
    companion object {
        private val logger = LoggerFactory.getLogger(TestModeImageGenerationStrategy::class.java)
    }

    init {
        logger.warn("======================================================")
        logger.warn("TEST MODE ACTIVE - Image generation will return mock data")
        logger.warn("Original images will be returned instead of AI-generated ones")
        logger.warn("======================================================")
    }

    override fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse {
        logger.info("TEST MODE: Generating {} mock images for prompt ID: {}", request.n, request.promptId)

        // Validate that the prompt exists (same as real implementation)
        val prompt = promptService.getPromptById(request.promptId)
        logger.debug("TEST MODE: Using prompt '{}' for mock generation", prompt.promptText)

        // Return the original image N times
        val originalImageBytes = imageFile.bytes
        val mockImageList =
            (1..request.n).map {
                logger.debug("TEST MODE: Creating mock image {} of {}", it, request.n)
                originalImageBytes.copyOf() // Create a copy to avoid reference issues
            }

        logger.info("TEST MODE: Successfully generated {} mock images", mockImageList.size)
        return ImageEditBytesResponse(imageBytes = mockImageList)
    }

    override fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info("TEST MODE: Testing prompt with master prompt: {}", request.masterPrompt)

        val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

        // Generate a mock URL that indicates this is test mode
        val mockImageUrl = "https://test-mode.voenix.shop/images/mock-${UUID.randomUUID()}.png"

        logger.info("TEST MODE: Generated mock image URL: {}", mockImageUrl)

        return TestPromptResponse(
            imageUrl = mockImageUrl,
            requestParams =
                TestPromptRequestParams(
                    model = "test-mode-mock",
                    size = request.size.apiValue,
                    n = 1,
                    responseFormat = "url",
                    masterPrompt = request.masterPrompt,
                    specificPrompt = request.specificPrompt,
                    combinedPrompt = combinedPrompt,
                    quality = request.quality.name.lowercase(),
                    background = request.background.name.lowercase(),
                ),
        )
    }
}
