package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.api.enums.ImageBackground
import com.jotoai.voenix.shop.image.api.enums.ImageQuality
import com.jotoai.voenix.shop.image.api.enums.ImageSize
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

class BaseImageGenerationServiceTest {
    private lateinit var openAIImageFacade: OpenAIImageFacade
    private lateinit var promptQueryService: PromptQueryService
    private lateinit var generatedImageRepository: GeneratedImageRepository
    private lateinit var baseImageGenerationService: TestableBaseImageGenerationService

    private lateinit var validImageFile: MultipartFile
    private lateinit var invalidImageFile: MultipartFile
    private lateinit var oversizedImageFile: MultipartFile

    @BeforeEach
    fun setUp() {
        openAIImageFacade = mock()
        promptQueryService = mock()
        generatedImageRepository = mock()

        baseImageGenerationService =
            TestableBaseImageGenerationService(
                openAIImageFacade = openAIImageFacade,
                promptService = promptQueryService,
                generatedImageRepository = generatedImageRepository,
            )

        // Create test files
        validImageFile =
            MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "valid content".toByteArray(),
            )

        invalidImageFile =
            MockMultipartFile(
                "image",
                "test-document.pdf",
                "application/pdf",
                "invalid content".toByteArray(),
            )

        // Create oversized file (over 10MB)
        val oversizedContent = ByteArray(11 * 1024 * 1024) // 11MB
        oversizedImageFile =
            MockMultipartFile(
                "image",
                "oversized-image.jpg",
                "image/jpeg",
                oversizedContent,
            )
    }

    // FILE VALIDATION TESTS

    @Test
    fun `validateImageFile should accept valid JPEG file`() {
        // When & Then - should not throw
        baseImageGenerationService.testValidateImageFile(validImageFile)
    }

    @Test
    fun `validateImageFile should accept PNG file`() {
        // Given
        val pngFile =
            MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                "valid png content".toByteArray(),
            )

        // When & Then - should not throw
        baseImageGenerationService.testValidateImageFile(pngFile)
    }

    @Test
    fun `validateImageFile should accept WebP file`() {
        // Given
        val webpFile =
            MockMultipartFile(
                "image",
                "test-image.webp",
                "image/webp",
                "valid webp content".toByteArray(),
            )

        // When & Then - should not throw
        baseImageGenerationService.testValidateImageFile(webpFile)
    }

    @Test
    fun `validateImageFile should accept JPG content type`() {
        // Given
        val jpgFile =
            MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpg",
                "valid jpg content".toByteArray(),
            )

        // When & Then - should not throw
        baseImageGenerationService.testValidateImageFile(jpgFile)
    }

    @Test
    fun `validateImageFile should reject empty file`() {
        // Given
        val emptyFile =
            MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                ByteArray(0),
            )

        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testValidateImageFile(emptyFile)
            }

        assertEquals("Image file is required", exception.message)
    }

    @Test
    fun `validateImageFile should reject oversized file`() {
        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testValidateImageFile(oversizedImageFile)
            }

        assertEquals("Image file size must be less than 10MB", exception.message)
    }

    @Test
    fun `validateImageFile should reject invalid content type`() {
        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testValidateImageFile(invalidImageFile)
            }

        assertEquals("Invalid image format. Allowed formats: JPEG, PNG, WebP", exception.message)
    }

    @Test
    fun `validateImageFile should reject null content type`() {
        // Given
        val fileWithNullContentType =
            MockMultipartFile(
                "image",
                "test.jpg",
                null,
                "content".toByteArray(),
            )

        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testValidateImageFile(fileWithNullContentType)
            }

        assertEquals("Invalid image format. Allowed formats: JPEG, PNG, WebP", exception.message)
    }

    // PROMPT VALIDATION TESTS

    @Test
    fun `validatePrompt should accept active prompt`() {
        // Given
        val promptId = 1L
        val activePrompt = createPrompt(id = promptId, active = true)

        whenever(promptQueryService.getPromptById(promptId)).thenReturn(activePrompt)

        // When & Then - should not throw
        baseImageGenerationService.testValidatePrompt(promptId)
    }

    @Test
    fun `validatePrompt should reject inactive prompt`() {
        // Given
        val promptId = 1L
        val inactivePrompt = createPrompt(id = promptId, active = false)

        whenever(promptQueryService.getPromptById(promptId)).thenReturn(inactivePrompt)

        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testValidatePrompt(promptId)
            }

        assertEquals("The selected prompt is not available", exception.message)
    }

    @Test
    fun `validatePrompt should handle non-existent prompt`() {
        // Given
        val promptId = 999L

        whenever(promptQueryService.getPromptById(promptId))
            .thenThrow(ResourceNotFoundException("Prompt not found"))

        // When & Then
        assertThrows<ResourceNotFoundException> {
            baseImageGenerationService.testValidatePrompt(promptId)
        }
    }

    // OPENAI REQUEST CREATION TESTS

    @Test
    fun `createOpenAIRequest should map all fields correctly`() {
        // Given
        val request =
            PublicImageGenerationRequest(
                promptId = 123L,
                background = ImageBackground.TRANSPARENT,
                quality = ImageQuality.HIGH,
                size = ImageSize.SQUARE_1024X1024,
                n = 2,
            )

        // When
        val result = baseImageGenerationService.testCreateOpenAIRequest(request)

        // Then
        assertEquals(123L, result.promptId)
        assertEquals(ImageBackground.TRANSPARENT, result.background)
        assertEquals(ImageQuality.HIGH, result.quality)
        assertEquals(ImageSize.SQUARE_1024X1024, result.size)
        assertEquals(2, result.n)
    }

    // RATE LIMIT TESTING

    @Test
    fun `checkTimeBasedRateLimit should allow generation when under limit`() {
        // Given
        val identifier = "test-user"
        val rateLimitHours = 1
        val maxGenerations = 5
        var capturedStartTime: LocalDateTime? = null

        val countFunction: (String, LocalDateTime) -> Long = { id, startTime ->
            capturedStartTime = startTime
            assertEquals(identifier, id)
            2L // Under the limit
        }

        // When & Then - should not throw
        baseImageGenerationService.testCheckTimeBasedRateLimit(
            identifier = identifier,
            rateLimitHours = rateLimitHours,
            maxGenerations = maxGenerations,
            countFunction = countFunction,
            rateLimitMessage = "Rate limit exceeded",
        )

        // Verify the time calculation
        assertNotNull(capturedStartTime)
    }

    @Test
    fun `checkTimeBasedRateLimit should reject when at limit`() {
        // Given
        val identifier = "test-user"
        val maxGenerations = 5
        val countFunction: (String, LocalDateTime) -> Long = { _, _ -> 5L } // At the limit

        // When & Then
        val exception =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testCheckTimeBasedRateLimit(
                    identifier = identifier,
                    rateLimitHours = 1,
                    maxGenerations = maxGenerations,
                    countFunction = countFunction,
                    rateLimitMessage = "Custom rate limit message",
                )
            }

        assertEquals("Custom rate limit message", exception.message)
    }

    @Test
    fun `checkTimeBasedRateLimit should reject when over limit`() {
        // Given
        val identifier = "test-user"
        val maxGenerations = 5
        val countFunction: (String, LocalDateTime) -> Long = { _, _ -> 10L } // Over the limit

        // When & Then
        assertThrows<BadRequestException> {
            baseImageGenerationService.testCheckTimeBasedRateLimit(
                identifier = identifier,
                rateLimitHours = 2,
                maxGenerations = maxGenerations,
                countFunction = countFunction,
                rateLimitMessage = "Rate limit exceeded for 2 hours",
            )
        }
    }

    // ERROR HANDLING TESTS

    @Test
    fun `executeWithErrorHandling should return result on success`() {
        // Given
        val expectedResult = "success"
        val operation = { expectedResult }

        // When
        val result =
            baseImageGenerationService.testExecuteWithErrorHandling(
                operation = operation,
                contextMessage = "test operation",
            )

        // Then
        assertEquals(expectedResult, result)
    }

    @Test
    fun `executeWithErrorHandling should preserve BadRequestException`() {
        // Given
        val exception = BadRequestException("Invalid request")
        val operation = { throw exception }

        // When & Then
        val thrownException =
            assertThrows<BadRequestException> {
                baseImageGenerationService.testExecuteWithErrorHandling(
                    operation = operation,
                    contextMessage = "test operation",
                )
            }

        assertEquals("Invalid request", thrownException.message)
    }

    @Test
    fun `executeWithErrorHandling should preserve ResourceNotFoundException`() {
        // Given
        val exception = ResourceNotFoundException("Resource not found")
        val operation = { throw exception }

        // When & Then
        val thrownException =
            assertThrows<ResourceNotFoundException> {
                baseImageGenerationService.testExecuteWithErrorHandling(
                    operation = operation,
                    contextMessage = "test operation",
                )
            }

        assertEquals("Resource not found", thrownException.message)
    }

    @Test
    fun `executeWithErrorHandling should wrap other exceptions as RuntimeException`() {
        // Given
        val exception = IllegalStateException("Unexpected error")
        val operation = { throw exception }

        // When & Then
        val thrownException =
            assertThrows<RuntimeException> {
                baseImageGenerationService.testExecuteWithErrorHandling(
                    operation = operation,
                    contextMessage = "test operation",
                )
            }

        assertEquals("Failed to generate image. Please try again later.", thrownException.message)
    }

    // HELPER METHODS

    private fun createPrompt(
        id: Long = 1L,
        active: Boolean = true,
        title: String = "Test Prompt",
        promptText: String = "Test Description",
    ): PromptDto =
        PromptDto(
            id = id,
            title = title,
            promptText = promptText,
            categoryId = null,
            category = null,
            subcategoryId = null,
            subcategory = null,
            active = active,
            slots = emptyList(),
            exampleImageUrl = null,
            createdAt = null,
            updatedAt = null,
        )

    /**
     * Testable concrete implementation of BaseImageGenerationService for testing purposes.
     * Exposes protected methods as public methods with 'test' prefix.
     */
    private class TestableBaseImageGenerationService(
        openAIImageFacade: OpenAIImageFacade,
        promptService: PromptQueryService,
        generatedImageRepository: GeneratedImageRepository,
    ) : BaseImageGenerationService(openAIImageFacade, promptService, generatedImageRepository) {
        fun testValidateImageFile(file: MultipartFile) = validateImageFile(file)

        fun testValidatePrompt(promptId: Long) = validatePrompt(promptId)

        fun testCreateOpenAIRequest(request: PublicImageGenerationRequest) = createOpenAIRequest(request)

        fun testCheckTimeBasedRateLimit(
            identifier: String,
            rateLimitHours: Int,
            maxGenerations: Int,
            countFunction: (String, LocalDateTime) -> Long,
            rateLimitMessage: String,
        ) = checkTimeBasedRateLimit(identifier, rateLimitHours, maxGenerations, countFunction, rateLimitMessage)

        fun <T> testExecuteWithErrorHandling(
            operation: () -> T,
            contextMessage: String,
        ): T = executeWithErrorHandling(operation, contextMessage)

        // Abstract method implementations (not used in these tests)
        override fun checkRateLimit(identifier: String) {
            // Test implementation - do nothing
        }

        override fun processImageGeneration(
            imageFile: MultipartFile,
            request: PublicImageGenerationRequest,
            identifier: String,
        ): PublicImageGenerationResponse {
            // Test implementation - return empty response
            return PublicImageGenerationResponse(emptyList(), emptyList())
        }
    }
}
