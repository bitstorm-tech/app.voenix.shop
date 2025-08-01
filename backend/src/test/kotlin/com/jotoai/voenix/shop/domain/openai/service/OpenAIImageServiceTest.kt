package com.jotoai.voenix.shop.domain.openai.service

import com.jotoai.voenix.shop.domain.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.domain.images.dto.ImageDto
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.ImageService
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageBackground
import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageQuality
import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class OpenAIImageServiceTest {
    private lateinit var imageService: ImageService
    private lateinit var imageGenerationStrategy: ImageGenerationStrategy
    private lateinit var openAIImageService: OpenAIImageService
    private lateinit var mockImageFile: MultipartFile

    @BeforeEach
    fun setUp() {
        imageService = mock(ImageService::class.java)
        imageGenerationStrategy = mock(ImageGenerationStrategy::class.java)
        openAIImageService = OpenAIImageService(imageService, imageGenerationStrategy)

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

        `when`(imageGenerationStrategy.generateImages(mockImageFile, request)).thenReturn(expectedResponse)

        // When
        val result = openAIImageService.editImageBytes(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.imageBytes.size)
        assertEquals(expectedImageBytes, result.imageBytes)

        verify(imageGenerationStrategy).generateImages(mockImageFile, request)
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
        `when`(imageGenerationStrategy.generateImages(mockImageFile, request)).thenReturn(strategResponse)

        // Mock image service responses
        val savedImage1 =
            ImageDto(
                filename = "saved-image-1.png",
                imageType = ImageType.PRIVATE,
            )

        val savedImage2 =
            ImageDto(
                filename = "saved-image-2.png",
                imageType = ImageType.PRIVATE,
            )

        `when`(imageService.store(any(MultipartFile::class.java), any(CreateImageRequest::class.java)))
            .thenReturn(savedImage1)
            .thenReturn(savedImage2)

        // When
        val result = openAIImageService.editImage(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.imageFilenames.size)
        assertEquals(listOf("saved-image-1.png", "saved-image-2.png"), result.imageFilenames)

        // Verify strategy was called
        verify(imageGenerationStrategy).generateImages(mockImageFile, request)

        // Verify image service was called twice for storage
        verify(imageService, times(2)).store(any(MultipartFile::class.java), any(CreateImageRequest::class.java))

        // Verify the CreateImageRequest was correct
        val imageRequestCaptor = ArgumentCaptor.forClass(CreateImageRequest::class.java)
        verify(imageService, times(2)).store(any(MultipartFile::class.java), imageRequestCaptor.capture())

        val capturedRequests = imageRequestCaptor.allValues
        capturedRequests.forEach { request ->
            assertEquals(ImageType.PRIVATE, request.imageType)
        }
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
        `when`(imageGenerationStrategy.generateImages(mockImageFile, request)).thenReturn(strategResponse)

        val savedImage =
            ImageDto(
                filename = "single-saved-image.png",
                imageType = ImageType.PRIVATE,
            )

        `when`(imageService.store(any(MultipartFile::class.java), any(CreateImageRequest::class.java)))
            .thenReturn(savedImage)

        // When
        val result = openAIImageService.editImage(mockImageFile, request)

        // Then
        assertEquals(1, result.imageFilenames.size)
        assertEquals("single-saved-image.png", result.imageFilenames[0])

        verify(imageGenerationStrategy).generateImages(mockImageFile, request)
        verify(imageService).store(any(MultipartFile::class.java), any(CreateImageRequest::class.java))
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

        `when`(imageGenerationStrategy.generateImages(mockImageFile, request))
            .thenThrow(RuntimeException("Strategy failed"))

        // When/Then
        assertThrows<RuntimeException> {
            openAIImageService.editImage(mockImageFile, request)
        }

        // Exception is thrown as expected
        verify(imageGenerationStrategy).generateImages(mockImageFile, request)
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
        `when`(imageGenerationStrategy.generateImages(mockImageFile, request)).thenReturn(strategResponse)

        `when`(imageService.store(any(MultipartFile::class.java), any(CreateImageRequest::class.java)))
            .thenThrow(RuntimeException("Storage failed"))

        // When/Then
        assertThrows<RuntimeException> {
            openAIImageService.editImage(mockImageFile, request)
        }

        // Exception is thrown as expected
        verify(imageGenerationStrategy).generateImages(mockImageFile, request)
        verify(imageService).store(any(MultipartFile::class.java), any(CreateImageRequest::class.java))
    }

    @Test
    fun `testPrompt should delegate to strategy`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Master prompt",
                specificPrompt = "Specific prompt",
                size = ImageSize.LANDSCAPE_1536X1024,
                quality = ImageQuality.HIGH,
                background = ImageBackground.AUTO,
            )

        val expectedResponse =
            TestPromptResponse(
                imageUrl = "https://test.example.com/image.png",
                requestParams =
                    TestPromptRequestParams(
                        model = "test-model",
                        size = request.size.apiValue,
                        n = 1,
                        responseFormat = "url",
                        masterPrompt = request.masterPrompt,
                        specificPrompt = request.specificPrompt,
                        combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}",
                        quality = request.quality.name.lowercase(),
                        background = request.background.name.lowercase(),
                    ),
            )

        `when`(imageGenerationStrategy.testPrompt(mockImageFile, request)).thenReturn(expectedResponse)

        // When
        val result = openAIImageService.testPrompt(mockImageFile, request)

        // Then
        assertNotNull(result)
        assertEquals(expectedResponse.imageUrl, result.imageUrl)
        assertEquals(expectedResponse.requestParams, result.requestParams)

        verify(imageGenerationStrategy).testPrompt(mockImageFile, request)
    }

    @Test
    fun `testPrompt should handle strategy failure`() {
        // Given
        val request =
            TestPromptRequest(
                masterPrompt = "Master prompt",
                specificPrompt = "Specific prompt",
                size = ImageSize.SQUARE_1024X1024,
                quality = ImageQuality.LOW,
                background = ImageBackground.OPAQUE,
            )

        `when`(imageGenerationStrategy.testPrompt(mockImageFile, request))
            .thenThrow(RuntimeException("Test prompt failed"))

        // When/Then
        assertThrows<RuntimeException> {
            openAIImageService.testPrompt(mockImageFile, request)
        }

        verify(imageGenerationStrategy).testPrompt(mockImageFile, request)
    }

    @Test
    fun `SimpleMultipartFile should work correctly`() {
        // Given
        val fileName = "test.png"
        val contentType = "image/png"
        val content = "test content".toByteArray()

        // Use reflection to access the private class (for testing purposes)
        val simpleMultipartFileClass =
            OpenAIImageService::class.java.declaredClasses
                .first { it.simpleName == "SimpleMultipartFile" }

        val constructor =
            simpleMultipartFileClass.getDeclaredConstructor(
                OpenAIImageService::class.java,
                String::class.java,
                String::class.java,
                ByteArray::class.java,
            )
        constructor.isAccessible = true

        val instance = constructor.newInstance(openAIImageService, fileName, contentType, content) as MultipartFile

        // Then
        assertEquals("file", instance.name)
        assertEquals(fileName, instance.originalFilename)
        assertEquals(contentType, instance.contentType)
        assertEquals(content.size.toLong(), instance.size)
        assertEquals(content.isEmpty(), instance.isEmpty)
        assertEquals(content, instance.bytes)
    }
}
