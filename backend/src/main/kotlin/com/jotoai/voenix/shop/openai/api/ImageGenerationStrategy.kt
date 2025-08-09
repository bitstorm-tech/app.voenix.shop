package com.jotoai.voenix.shop.openai.api

import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.api.dto.TestPromptResponse
import org.springframework.web.multipart.MultipartFile

/**
 * Strategy interface for image generation implementations.
 * Allows switching between different image generation backends (OpenAI, test mode, etc.)
 * This interface is part of the public API to allow external modules to provide custom strategies.
 */
interface ImageGenerationStrategy {
    /**
     * Generates edited images based on the provided request and returns raw image bytes.
     * This allows the caller to handle storage using their preferred strategy.
     *
     * @param imageFile The image file to be edited
     * @param request The image edit request containing generation parameters
     * @return Response containing raw image bytes
     */
    fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse

    /**
     * Tests a prompt with the image generation service and returns the results.
     *
     * @param imageFile The image file to test with
     * @param request The prompt test request
     * @return Response containing test results
     */
    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse
}
