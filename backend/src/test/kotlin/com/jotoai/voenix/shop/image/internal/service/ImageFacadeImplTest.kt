package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.domain.GeneratedImage
import com.jotoai.voenix.shop.image.internal.domain.UploadedImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

class ImageFacadeImplTest {
    private lateinit var imageService: ImageService

    private lateinit var imageFacade: ImageFacadeImpl

    private lateinit var mockFile: MultipartFile

    @BeforeEach
    fun setUp() {
        imageService = mock()

        imageFacade =
            ImageFacadeImpl(
                imageService = imageService,
            )

        mockFile =
            MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".toByteArray(),
            )
    }

    // UPLOADED IMAGE TESTS

    @Test
    fun `createUploadedImage should store image`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val storedFilename = "stored-file.jpg"
        val fileSize = 1024L
        val uploadedImage =
            createUploadedImage(
                id = 100L,
                uuid = uuid,
                storedFilename = storedFilename,
                userId = userId,
                fileSize = fileSize,
            )

        val expectedDto =
            com.jotoai.voenix.shop.image.api.dto.UploadedImageDto(
                filename = storedFilename,
                imageType = ImageType.PRIVATE,
                uuid = uuid,
                originalFilename = "test-image.jpg",
                contentType = "image/jpeg",
                fileSize = fileSize,
                uploadedAt = LocalDateTime.now(),
            )

        whenever(imageService.createUploadedImage(mockFile, userId)).thenReturn(expectedDto)

        // When
        val result = imageFacade.createUploadedImage(mockFile, userId)

        // Then
        assertNotNull(result)
        assertEquals(storedFilename, result.filename)
        assertEquals(ImageType.PRIVATE, result.imageType)
        assertEquals(uuid, result.uuid)
        assertEquals("test-image.jpg", result.originalFilename)
        assertEquals("image/jpeg", result.contentType)
        assertEquals(fileSize, result.fileSize)

        verify(imageService).createUploadedImage(mockFile, userId)
    }

    @Test
    fun `createUploadedImage should throw exception when image ID is null`() {
        // Given
        val userId = 1L
        val uploadedImage = createUploadedImage(id = null, userId = userId)

        whenever(imageService.createUploadedImage(mockFile, userId)).thenThrow(ImageStorageException("Image ID not generated"))

        // When & Then
        assertThrows<ImageStorageException> {
            imageFacade.createUploadedImage(mockFile, userId)
        }
    }

    @Test
    fun `createUploadedImage should handle storage service exception`() {
        // Given
        val userId = 1L
        whenever(imageService.createUploadedImage(mockFile, userId))
            .thenThrow(ImageStorageException("Storage failed"))

        // When & Then
        val exception =
            assertThrows<ImageStorageException> {
                imageFacade.createUploadedImage(mockFile, userId)
            }

        assertEquals("Storage failed", exception.message)
    }

    @Test
    fun `getUploadedImageByUuid should return image when found`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val uploadedImage = createUploadedImage(uuid = uuid, userId = userId)

        val expectedDto =
            com.jotoai.voenix.shop.image.api.dto.UploadedImageDto(
                filename = uploadedImage.storedFilename,
                imageType = ImageType.PRIVATE,
                uuid = uuid,
                originalFilename = uploadedImage.originalFilename,
                contentType = uploadedImage.contentType,
                fileSize = uploadedImage.fileSize,
                uploadedAt = uploadedImage.uploadedAt,
            )

        whenever(imageService.getUploadedImageByUuid(uuid, userId)).thenReturn(expectedDto)

        // When
        val result = imageFacade.getUploadedImageByUuid(uuid, userId)

        // Then
        assertEquals(uploadedImage.storedFilename, result.filename)
        assertEquals(ImageType.PRIVATE, result.imageType)
        assertEquals(uuid, result.uuid)
    }

    @Test
    fun `getUploadedImageByUuid should throw exception when not found`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()

        whenever(imageService.getUploadedImageByUuid(uuid, userId)).thenThrow(ImageNotFoundException("Image not found"))

        // When & Then
        assertThrows<ImageNotFoundException> {
            imageFacade.getUploadedImageByUuid(uuid, userId)
        }
    }

    @Test
    fun `deleteUploadedImage should delete from storage and database`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val uploadedImage = createUploadedImage(id = 100L, uuid = uuid, userId = userId)

        // When
        imageFacade.deleteUploadedImage(uuid, userId)

        // Then
        verify(imageService).deleteUploadedImage(uuid, userId)
    }

    @Test
    fun `deleteUploadedImage should throw exception when not found`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()

        whenever(imageService.deleteUploadedImage(uuid, userId)).thenThrow(ImageNotFoundException("Image not found"))

        // When & Then
        assertThrows<ImageNotFoundException> {
            imageFacade.deleteUploadedImage(uuid, userId)
        }
    }

    @Test
    fun `deleteUploadedImage should handle deletion failure`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val uploadedImage = createUploadedImage(id = 100L, uuid = uuid, userId = userId)

        doThrow(ImageStorageException("Delete failed")).`when`(imageService).deleteUploadedImage(uuid, userId)

        // When & Then
        val exception =
            assertThrows<ImageStorageException> {
                imageFacade.deleteUploadedImage(uuid, userId)
            }

        assertEquals("Delete failed", exception.message)
    }

    @Test
    fun `getUserUploadedImages should return list of images`() {
        // Given
        val userId = 1L
        val images =
            listOf(
                createUploadedImage(uuid = UUID.randomUUID(), userId = userId),
                createUploadedImage(uuid = UUID.randomUUID(), userId = userId),
            )

        val expectedDtos =
            images.map { image ->
                com.jotoai.voenix.shop.image.api.dto.UploadedImageDto(
                    filename = image.storedFilename,
                    imageType = ImageType.PRIVATE,
                    uuid = image.uuid,
                    originalFilename = image.originalFilename,
                    contentType = image.contentType,
                    fileSize = image.fileSize,
                    uploadedAt = image.uploadedAt,
                )
            }

        whenever(imageService.getUserUploadedImages(userId)).thenReturn(expectedDtos)

        // When
        val result = imageFacade.getUserUploadedImages(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals(images[0].storedFilename, result[0].filename)
        assertEquals(images[1].storedFilename, result[1].filename)
        result.forEach { assertEquals(ImageType.PRIVATE, it.imageType) }
    }

    @Test
    fun `getUserUploadedImages should return empty list when no images found`() {
        // Given
        val userId = 1L

        whenever(imageService.getUserUploadedImages(userId)).thenReturn(emptyList())

        // When
        val result = imageFacade.getUserUploadedImages(userId)

        // Then
        assertEquals(0, result.size)
    }

    // GENERATED IMAGE TESTS

    @Test
    fun `getGeneratedImageByUuid should return image when found with userId`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(uuid = uuid, userId = userId)

        val expectedDto =
            com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto(
                filename = generatedImage.filename,
                imageType = ImageType.GENERATED,
                promptId = generatedImage.promptId,
                userId = userId,
                generatedAt = generatedImage.generatedAt,
                ipAddress = generatedImage.ipAddress,
            )

        whenever(imageService.getGeneratedImageByUuid(uuid, userId)).thenReturn(expectedDto)

        // When
        val result = imageFacade.getGeneratedImageByUuid(uuid, userId)

        // Then
        assertEquals(generatedImage.filename, result.filename)
        assertEquals(ImageType.GENERATED, result.imageType)
        assertEquals(generatedImage.promptId, result.promptId)
        assertEquals(userId, result.userId)
    }

    @Test
    fun `getGeneratedImageByUuid should return image when found without userId`() {
        // Given
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(uuid = uuid, userId = null)

        val expectedDto =
            com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto(
                filename = generatedImage.filename,
                imageType = ImageType.GENERATED,
                promptId = generatedImage.promptId,
                userId = null,
                generatedAt = generatedImage.generatedAt,
                ipAddress = generatedImage.ipAddress,
            )

        whenever(imageService.getGeneratedImageByUuid(uuid, null)).thenReturn(expectedDto)

        // When
        val result = imageFacade.getGeneratedImageByUuid(uuid, null)

        // Then
        assertEquals(generatedImage.filename, result.filename)
        assertEquals(ImageType.GENERATED, result.imageType)
    }

    @Test
    fun `getGeneratedImageByUuid should throw exception when not found with userId`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()

        whenever(imageService.getGeneratedImageByUuid(uuid, userId)).thenThrow(ImageNotFoundException("Image not found"))

        // When & Then
        assertThrows<ImageNotFoundException> {
            imageFacade.getGeneratedImageByUuid(uuid, userId)
        }
    }

    @Test
    fun `updateGeneratedImage should update image`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(id = 100L, uuid = uuid, userId = userId)
        val updateRequest = UpdateGeneratedImageRequest(promptId = 999L, ipAddress = "192.168.1.1")

        val expectedDto =
            com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto(
                filename = generatedImage.filename,
                imageType = ImageType.GENERATED,
                promptId = 999L,
                userId = userId,
                generatedAt = generatedImage.generatedAt,
                ipAddress = "192.168.1.1",
            )

        whenever(imageService.updateGeneratedImage(uuid, updateRequest, userId)).thenReturn(expectedDto)

        // When
        val result = imageFacade.updateGeneratedImage(uuid, updateRequest, userId)

        // Then
        assertEquals(999L, result.promptId)
        assertEquals("192.168.1.1", result.ipAddress)
        verify(imageService).updateGeneratedImage(uuid, updateRequest, userId)
    }

    @Test
    fun `updateGeneratedImage should handle repository save failure`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(id = 100L, uuid = uuid, userId = userId)
        val updateRequest = UpdateGeneratedImageRequest(promptId = 999L)

        whenever(imageService.updateGeneratedImage(uuid, updateRequest, userId))
            .thenThrow(ImageStorageException("Save failed"))

        // When & Then
        val exception =
            assertThrows<ImageStorageException> {
                imageFacade.updateGeneratedImage(uuid, updateRequest, userId)
            }

        assertEquals("Save failed", exception.message)
    }

    @Test
    fun `deleteGeneratedImage should delete from storage and database`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(id = 100L, uuid = uuid, userId = userId)

        // When
        imageFacade.deleteGeneratedImage(uuid, userId)

        // Then
        verify(imageService).deleteGeneratedImage(uuid, userId)
    }

    @Test
    fun `deleteGeneratedImage should handle deletion failure`() {
        // Given
        val userId = 1L
        val uuid = UUID.randomUUID()
        val generatedImage = createGeneratedImage(id = 100L, uuid = uuid, userId = userId)

        doThrow(ImageStorageException("Delete failed")).`when`(imageService).deleteGeneratedImage(uuid, userId)

        // When & Then
        val exception =
            assertThrows<ImageStorageException> {
                imageFacade.deleteGeneratedImage(uuid, userId)
            }

        assertEquals("Delete failed", exception.message)
    }

    @Test
    fun `getUserGeneratedImages should return list of images`() {
        // Given
        val userId = 1L
        val images =
            listOf(
                createGeneratedImage(uuid = UUID.randomUUID(), userId = userId),
                createGeneratedImage(uuid = UUID.randomUUID(), userId = userId),
            )

        val expectedDtos =
            images.map { image ->
                com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto(
                    filename = image.filename,
                    imageType = ImageType.GENERATED,
                    promptId = image.promptId,
                    userId = image.userId,
                    generatedAt = image.generatedAt,
                    ipAddress = image.ipAddress,
                )
            }

        whenever(imageService.getUserGeneratedImages(userId)).thenReturn(expectedDtos)

        // When
        val result = imageFacade.getUserGeneratedImages(userId)

        // Then
        assertEquals(2, result.size)
        assertEquals(images[0].filename, result[0].filename)
        assertEquals(images[1].filename, result[1].filename)
        result.forEach { assertEquals(ImageType.GENERATED, it.imageType) }
    }

    // UNSUPPORTED OPERATION TESTS

    @Test
    fun `createImage should throw UnsupportedOperationException`() {
        // When & Then
        assertThrows<UnsupportedOperationException> {
            imageFacade.createImage(mock())
        }
    }

    // HELPER METHODS

    private fun createUploadedImage(
        id: Long? = 1L,
        uuid: UUID = UUID.randomUUID(),
        originalFilename: String = "test-image.jpg",
        storedFilename: String = "stored-image.jpg",
        contentType: String = "image/jpeg",
        fileSize: Long = 1024L,
        userId: Long = 1L,
        uploadedAt: LocalDateTime = LocalDateTime.now(),
    ): UploadedImage =
        UploadedImage(
            id = id,
            uuid = uuid,
            originalFilename = originalFilename,
            storedFilename = storedFilename,
            contentType = contentType,
            fileSize = fileSize,
            userId = userId,
            uploadedAt = uploadedAt,
        )

    private fun createGeneratedImage(
        id: Long? = 1L,
        uuid: UUID = UUID.randomUUID(),
        filename: String = "generated-image.jpg",
        promptId: Long = 100L,
        userId: Long? = 1L,
        generatedAt: LocalDateTime = LocalDateTime.now(),
        ipAddress: String? = "127.0.0.1",
    ): GeneratedImage =
        GeneratedImage(
            id = id,
            uuid = uuid,
            filename = filename,
            promptId = promptId,
            userId = userId,
            generatedAt = generatedAt,
            ipAddress = ipAddress,
        )
}
