package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.api.enums.ImageBackground
import com.jotoai.voenix.shop.openai.api.enums.ImageQuality
import com.jotoai.voenix.shop.openai.api.enums.ImageSize
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class TestModeImageGenerationStrategyTest {
    private lateinit var promptQueryService: PromptQueryService
    private lateinit var testModeStrategy: TestModeImageGenerationStrategy
    private lateinit var mockImageFile: MultipartFile

    @BeforeEach
    fun setUp() {
        promptQueryService = mockk()
        testModeStrategy = TestModeImageGenerationStrategy(promptQueryService)

        // Create a mock image file with some test data
        mockImageFile =
            MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                "test image content".toByteArray(),
            )
    }

    @Test
    fun `generateImages should return original image N times`() {
        // Given
        val promptId = 1L
        val numberOfImages = 3
        val request =
            CreateImageEditRequest(
                promptId = promptId,
                n = numberOfImages,
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        val mockPrompt =
            PromptDto(
                id = promptId,
                title = "Test Prompt",
                promptText = "Test prompt text",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(promptId) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(numberOfImages, result.imageBytes.size)

        // Verify all images contain the same content as original
        val originalImageBytes = mockImageFile.bytes
        result.imageBytes.forEach { imageBytes ->
            assertTrue(imageBytes.contentEquals(originalImageBytes))
        }

        // Verify prompt service was called
        verify { promptQueryService.getPromptById(promptId) }
    }

    @Test
    fun `generateImages should create separate copies of image bytes`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 2,
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.LOW,
                background = ImageBackground.OPAQUE,
            )

        val mockPrompt =
            PromptDto(
                id = 1L,
                title = "Test Prompt",
                promptText = "Test prompt",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(1L) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(mockImageFile, request)

        // Then
        assertEquals(2, result.imageBytes.size)

        // Verify they are separate instances (not same reference)
        val firstImage = result.imageBytes[0]
        val secondImage = result.imageBytes[1]

        // Content should be equal but not same instance
        assertTrue(firstImage.contentEquals(secondImage))
        assertTrue(firstImage !== secondImage) // Different references
    }

    @Test
    fun `generateImages should handle single image generation`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 1,
                size = ImageSize.PORTRAIT_1024X1536,
                quality = ImageQuality.MEDIUM,
                background = ImageBackground.TRANSPARENT,
            )

        val mockPrompt =
            PromptDto(
                id = 1L,
                title = "Single Image Test",
                promptText = "Single image test",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(1L) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(mockImageFile, request)

        // Then
        assertEquals(1, result.imageBytes.size)
        assertTrue(result.imageBytes[0].contentEquals(mockImageFile.bytes))
    }

    @Test
    fun `generateImages should handle maximum number of images`() {
        // Given
        val maxImages = 10
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = maxImages,
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        val mockPrompt =
            PromptDto(
                id = 1L,
                title = "Max Images Test",
                promptText = "Max images test",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(1L) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(mockImageFile, request)

        // Then
        assertEquals(maxImages, result.imageBytes.size)
        result.imageBytes.forEach { imageBytes ->
            assertTrue(imageBytes.contentEquals(mockImageFile.bytes))
        }
    }

    @Test
    fun `generateImages should throw exception when prompt not found`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 999L,
                n = 1,
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        every { promptQueryService.getPromptById(999L) } throws ResourceNotFoundException("Prompt not found")

        // When/Then
        assertThrows<ResourceNotFoundException> {
            testModeStrategy.generateImages(mockImageFile, request)
        }

        verify { promptQueryService.getPromptById(999L) }
    }

    @Test
    fun `generateImages should handle different image formats`() {
        // Given
        val jpegImageFile =
            MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "jpeg test content".toByteArray(),
            )

        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 2,
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.LOW,
                background = ImageBackground.OPAQUE,
            )

        val mockPrompt =
            PromptDto(
                id = 1L,
                title = "JPEG Test",
                promptText = "JPEG test",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(1L) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(jpegImageFile, request)

        // Then
        assertEquals(2, result.imageBytes.size)
        result.imageBytes.forEach { imageBytes ->
            assertTrue(imageBytes.contentEquals(jpegImageFile.bytes))
        }
    }

    @Test
    fun `generateImages should handle empty image file`() {
        // Given
        val emptyImageFile =
            MockMultipartFile(
                "image",
                "empty.png",
                "image/png",
                ByteArray(0),
            )

        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 1,
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.LOW,
                background = ImageBackground.OPAQUE,
            )

        val mockPrompt =
            PromptDto(
                id = 1L,
                title = "Empty Image Test",
                promptText = "Empty image test",
                categoryId = null,
                category = null,
                subcategoryId = null,
                subcategory = null,
                active = true,
                slots = emptyList(),
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        every { promptQueryService.getPromptById(1L) } returns mockPrompt

        // When
        val result = testModeStrategy.generateImages(emptyImageFile, request)

        // Then
        assertEquals(1, result.imageBytes.size)
        assertEquals(0, result.imageBytes[0].size)
    }

    @Test
    fun `testPrompt should return mock response with generated URL`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Master prompt text",
                specificPrompt = "Specific prompt text",
                sizeString = "1536x1024",
                qualityString = "HIGH",
                backgroundString = "AUTO",
            )

        // When
        val result = testModeStrategy.testPrompt(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertNotNull(result.imageUrl)
        assertTrue(result.imageUrl.startsWith("https://test-mode.voenix.shop/images/mock-"))
        assertTrue(result.imageUrl.endsWith(".png"))

        // Verify the URL contains a UUID pattern
        val uuidPart = result.imageUrl.substringAfter("mock-").substringBefore(".png")
        // Basic UUID format validation (just check it's not empty and has reasonable length)
        assertTrue(uuidPart.isNotEmpty())
        assertTrue(uuidPart.length >= 30) // UUIDs are typically 36 chars, but after processing might be different

        // Verify request params
        val params = result.requestParams
        assertEquals("test-mode-mock", params.model)
        assertEquals(request.getSize().apiValue, params.size)
        assertEquals(1, params.n)
        assertEquals("url", params.responseFormat)
        assertEquals(request.masterPrompt, params.masterPrompt)
        assertEquals(request.specificPrompt, params.specificPrompt)
        assertEquals("${request.masterPrompt} ${request.specificPrompt}", params.combinedPrompt)
        assertEquals(request.getQuality().name.lowercase(), params.quality)
        assertEquals(request.getBackground().name.lowercase(), params.background)
    }

    @Test
    fun `testPrompt should handle empty specific prompt`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Master prompt only",
                specificPrompt = "",
                sizeString = "1024x1024",
                qualityString = "LOW",
                backgroundString = "OPAQUE",
            )

        // When
        val result = testModeStrategy.testPrompt(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals("Master prompt only", result.requestParams.combinedPrompt)
    }

    @Test
    fun `testPrompt should handle empty master prompt`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "",
                specificPrompt = "Specific prompt only",
                sizeString = "1024x1536",
                qualityString = "MEDIUM",
                backgroundString = "TRANSPARENT",
            )

        // When
        val result = testModeStrategy.testPrompt(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals("Specific prompt only", result.requestParams.combinedPrompt)
    }

    @Test
    fun `testPrompt should combine prompts with proper spacing`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Create a beautiful",
                specificPrompt = "landscape painting",
                sizeString = "1536x1024",
                qualityString = "HIGH",
                backgroundString = "AUTO",
            )

        // When
        val result = testModeStrategy.testPrompt(mockImageFile, request)

        // Then
        assertEquals("Create a beautiful landscape painting", result.requestParams.combinedPrompt)
    }

    @Test
    fun `testPrompt should generate unique URLs for multiple calls`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Test prompt",
                specificPrompt = "for uniqueness",
                sizeString = "1024x1024",
                qualityString = "LOW",
                backgroundString = "OPAQUE",
            )

        // When
        val result1 = testModeStrategy.testPrompt(mockImageFile, request)
        val result2 = testModeStrategy.testPrompt(mockImageFile, request)

        // Then
        assertNotNull(result1.imageUrl)
        assertNotNull(result2.imageUrl)
        assertTrue(result1.imageUrl != result2.imageUrl) // Should be different URLs

        // Both should be valid test mode URLs
        assertTrue(result1.imageUrl.startsWith("https://test-mode.voenix.shop/images/mock-"))
        assertTrue(result2.imageUrl.startsWith("https://test-mode.voenix.shop/images/mock-"))
    }
}
