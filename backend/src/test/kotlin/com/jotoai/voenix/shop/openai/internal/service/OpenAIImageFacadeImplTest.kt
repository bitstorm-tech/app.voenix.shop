package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.openai.api.enums.ImageBackground
import com.jotoai.voenix.shop.openai.api.enums.ImageQuality
import com.jotoai.voenix.shop.openai.api.enums.ImageSize
import com.jotoai.voenix.shop.openai.api.ImageGenerationStrategy
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageEditBytesResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

class OpenAIImageFacadeImplTest {
    private lateinit var imageService: ImageService
    private lateinit var imageGenerationStrategy: ImageGenerationStrategy
    private lateinit var openAIImageFacade: OpenAIImageFacadeImpl
    private lateinit var mockImageFile: MultipartFile

    @BeforeEach
    fun setUp() {
        imageService = mockk()
        imageGenerationStrategy = mockk()
        openAIImageFacade = OpenAIImageFacadeImpl(imageService, imageGenerationStrategy)

        mockImageFile =
            MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                "test image content".toByteArray(),
            )
    }

    @Test
    fun `editImageBytes should delegate to strategy and return raw bytes`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 2,
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        val expectedImageBytes =
            listOf(
                "image1".toByteArray(),
                "image2".toByteArray(),
            )

        val expectedResponse = ImageEditBytesResponse(imageBytes = expectedImageBytes)

        every { imageGenerationStrategy.generateImages(mockImageFile, request) } returns expectedResponse

        // When
        val result = openAIImageFacade.editImageBytes(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.imageBytes.size)
        assertEquals(expectedImageBytes, result.imageBytes)

        verify { imageGenerationStrategy.generateImages(mockImageFile, request) }
    }

    @Test
    fun `editImage should generate images and store them`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 2,
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.MEDIUM,
                background = ImageBackground.OPAQUE,
            )

        val generatedImageBytes =
            listOf(
                "generated-image-1".toByteArray(),
                "generated-image-2".toByteArray(),
            )

        val strategResponse = ImageEditBytesResponse(imageBytes = generatedImageBytes)
        every { imageGenerationStrategy.generateImages(mockImageFile, request) } returns strategResponse

        // Mock image service responses
        val savedFilename1 = "saved-image-1.png"
        val savedFilename2 = "saved-image-2.png"

        val mockDto1 =
            UploadedImageDto(
                filename = savedFilename1,
                imageType = ImageType.PRIVATE,
                uuid = UUID.randomUUID(),
                originalFilename = "image1.png",
                contentType = "image/png",
                fileSize = 1024L,
                uploadedAt = LocalDateTime.now(),
            )
        val mockDto2 =
            UploadedImageDto(
                filename = savedFilename2,
                imageType = ImageType.PRIVATE,
                uuid = UUID.randomUUID(),
                originalFilename = "image2.png",
                contentType = "image/png",
                fileSize = 1024L,
                uploadedAt = LocalDateTime.now(),
            )

        // Use doReturn to avoid issues with consecutive calls
        every { imageService.store(any(), any()) } returnsMany listOf(mockDto1, mockDto2)

        // When
        val result = openAIImageFacade.editImage(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.imageFilenames.size)
        assertEquals(listOf("saved-image-1.png", "saved-image-2.png"), result.imageFilenames)

        // Verify strategy was called
        verify { imageGenerationStrategy.generateImages(mockImageFile, request) }
        verify(exactly = 2) { imageService.store(any(), any()) }
    }

    @Test
    fun `editImage should handle single image generation`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 1,
                size = ImageSize.PORTRAIT_1024X1536,
                quality = ImageQuality.LOW,
                background = ImageBackground.TRANSPARENT,
            )

        val generatedImageBytes = listOf("single-generated-image".toByteArray())
        val strategResponse = ImageEditBytesResponse(imageBytes = generatedImageBytes)
        every { imageGenerationStrategy.generateImages(mockImageFile, request) } returns strategResponse

        val savedFilename = "single-saved-image.png"
        val mockDto =
            UploadedImageDto(
                filename = savedFilename,
                imageType = ImageType.PRIVATE,
                uuid = UUID.randomUUID(),
                originalFilename = "single.png",
                contentType = "image/png",
                fileSize = 1024L,
                uploadedAt = LocalDateTime.now(),
            )

        every { imageService.store(any(), any()) } returns mockDto

        // When
        val result = openAIImageFacade.editImage(mockImageFile, request)

        // Then
        assertEquals(1, result.imageFilenames.size)
        assertEquals("single-saved-image.png", result.imageFilenames[0])

        verify { imageGenerationStrategy.generateImages(mockImageFile, request) }
        verify { imageService.store(any(), any()) }
    }

    @Test
    fun `editImage should handle strategy failure`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 1,
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        every {
            imageGenerationStrategy.generateImages(mockImageFile, request)
        } throws RuntimeException("Strategy failed")

        // When/Then
        assertThrows<RuntimeException> {
            openAIImageFacade.editImage(mockImageFile, request)
        }

        // Exception is thrown as expected
        verify { imageGenerationStrategy.generateImages(mockImageFile, request) }
    }

    @Test
    fun `editImage should handle storage failure`() {
        // Given
        val request =
            CreateImageEditRequest(
                promptId = 1L,
                n = 1,
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.MEDIUM,
                background = ImageBackground.OPAQUE,
            )

        val generatedImageBytes = listOf("generated-image".toByteArray())
        val strategResponse = ImageEditBytesResponse(imageBytes = generatedImageBytes)
        every { imageGenerationStrategy.generateImages(mockImageFile, request) } returns strategResponse

        every { imageService.store(any(), any()) } throws RuntimeException("Storage failed")

        // When/Then
        assertThrows<RuntimeException> {
            openAIImageFacade.editImage(mockImageFile, request)
        }

        // Exception is thrown as expected
        verify { imageGenerationStrategy.generateImages(mockImageFile, request) }
        verify { imageService.store(any(), any()) }
    }
}
