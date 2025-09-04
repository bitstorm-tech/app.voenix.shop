package com.jotoai.voenix.shop.openai.internal.provider

import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.exception.ImageGenerationException
import com.jotoai.voenix.shop.prompt.PromptDto
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Component
internal class FluxImageProvider(
    @Value($$"${FLUX_API_KEY:}") private val fluxApiKey: String,
) : ImageGenerationProvider {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun generateImages(
        imageFile: MultipartFile,
        prompt: PromptDto,
        options: GenerationOptions,
    ): ImageEditBytesResponse {
        logger.info { "Starting Flux AI image generation with prompt ID: ${prompt.id}" }

        if (fluxApiKey.isEmpty()) {
            throw ImageGenerationException("Flux API key not configured")
        }

        return executeApiCall("Flux image generation") {
            val promptText = buildFinalPrompt(prompt)

            // Placeholder for Flux API implementation
            // This will be implemented when Flux API credentials are available
            logger.warn { "Flux provider implementation pending - using mock response" }

            // Return mock data for now
            val mockImageBytes = imageFile.bytes
            val imageList = (1..options.n).map { mockImageBytes.copyOf() }

            ImageEditBytesResponse(imageBytes = imageList)
        }
    }

    override suspend fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "Starting Flux AI prompt test with master prompt: ${request.masterPrompt}" }

        if (fluxApiKey.isEmpty()) {
            throw ImageGenerationException("Flux API key not configured")
        }

        return executeApiCall("Flux prompt testing") {
            val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

            // Placeholder for Flux API prompt testing
            logger.warn { "Flux provider prompt test implementation pending - using mock response" }

            val mockImageUrl = "https://test-mode.voenix.shop/images/flux-mock-${UUID.randomUUID()}.png"

            TestPromptResponse(
                imageUrl = mockImageUrl,
                requestParams =
                    TestPromptRequestParams(
                        model = "flux-1",
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

    private fun buildFinalPrompt(prompt: PromptDto): String {
        val mainPrompt = listOfNotNull(prompt.promptText)
        val slotPrompts =
            prompt.slots
                .sortedBy { it.promptSlotType?.position ?: 0 }
                .mapNotNull { it.prompt }

        return (mainPrompt + slotPrompts)
            .joinToString(" ")
            .trim()
    }

    private inline fun <T> executeApiCall(
        operation: String,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "$operation failed" }
            throw ImageGenerationException("$operation failed: ${e.message}", e)
        }
}
