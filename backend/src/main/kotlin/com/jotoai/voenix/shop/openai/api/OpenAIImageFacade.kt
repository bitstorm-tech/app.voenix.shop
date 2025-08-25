package com.jotoai.voenix.shop.openai.api

import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.api.dto.ImageEditResponse
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.api.dto.TestPromptResponse
import org.springframework.web.multipart.MultipartFile

/**
 * Facade interface for OpenAI image generation operations.
 * This interface provides the main entry points for image editing and generation.
 */
interface OpenAIImageFacade {
    /**
     * Edits an image and returns raw image bytes without storing them.
     * Delegates to the configured ImageGenerationStrategy.
     *
     * @param imageFile The image file to be edited
     * @param request The image edit request containing generation parameters
     * @return Response containing raw image bytes
     */
    fun editImageBytes(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse

    /**
     * Edits an image from bytes and returns raw image bytes without storing them.
     * Delegates to the configured ImageGenerationStrategy.
     *
     * @param imageBytes The image bytes to be edited
     * @param request The image edit request containing generation parameters
     * @return Response containing raw image bytes
     */
    fun editImageBytes(
        imageBytes: ByteArray,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse

    /**
     * Edits an image and stores the results, returning filenames.
     * Uses the strategy pattern for generation and then handles storage.
     *
     * @param imageFile The image file to be edited
     * @param request The image edit request containing generation parameters
     * @return Response containing stored image filenames
     */
    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse

    /**
     * Tests a prompt with the configured generation strategy.
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
