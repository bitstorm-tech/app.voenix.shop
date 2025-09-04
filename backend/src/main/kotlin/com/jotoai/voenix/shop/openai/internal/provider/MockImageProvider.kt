package com.jotoai.voenix.shop.openai.internal.provider

import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Component
internal class MockImageProvider : ImageGenerationProvider {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun generateImages(
        imageFile: MultipartFile,
        prompt: PromptDto,
        options: GenerationOptions,
    ): ImageEditBytesResponse {
        logger.info { "TEST MODE: Generating ${options.n} mock images for prompt ID: ${prompt.id}" }
        logger.debug { "TEST MODE: Using prompt '${prompt.promptText}' for mock generation" }

        // Return the original image N times
        val originalImageBytes = imageFile.bytes
        val mockImageList =
            (1..options.n).map {
                logger.debug { "TEST MODE: Creating mock image $it of ${options.n}" }
                originalImageBytes.copyOf() // Create a copy to avoid reference issues
            }

        logger.info { "TEST MODE: Successfully generated ${mockImageList.size} mock images" }
        return ImageEditBytesResponse(imageBytes = mockImageList)
    }

    override suspend fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "TEST MODE: Testing prompt with master prompt: ${request.masterPrompt}" }

        val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()
        val mockImageUrl = "https://test-mode.voenix.shop/images/mock-${UUID.randomUUID()}.png"

        logger.info { "TEST MODE: Generated mock image URL: $mockImageUrl" }

        return TestPromptResponse(
            imageUrl = mockImageUrl,
            requestParams =
                TestPromptRequestParams(
                    model = "test-mode-mock",
                    size = request.getSize().apiValue,
                    n = 1,
                    responseFormat = "url",
                    masterPrompt = request.masterPrompt,
                    specificPrompt = request.specificPrompt,
                    combinedPrompt = combinedPrompt,
                    quality = request.getQuality().apiValue,
                    background = request.getBackground().apiValue,
                ),
        )
    }
}
