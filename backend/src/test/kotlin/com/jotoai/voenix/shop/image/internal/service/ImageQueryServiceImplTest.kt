package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.UUID

class ImageQueryServiceImplTest {
    private lateinit var generatedImageRepository: GeneratedImageRepository
    private lateinit var uploadedImageRepository: UploadedImageRepository
    private lateinit var imageQueryService: ImageQueryServiceImpl

    @BeforeEach
    fun setUp() {
        generatedImageRepository = mock()
        uploadedImageRepository = mock()

        imageQueryService =
            ImageQueryServiceImpl(
                generatedImageRepository = generatedImageRepository,
                uploadedImageRepository = uploadedImageRepository,
            )
    }

    // FIND IMAGE BY FILENAME TESTS

    @Test
    fun `findImageByFilename should return SimpleImageDto when generated image found`() {
        // Given
        val filename = "generated-image.jpg"
        val generatedImage = createGeneratedImage(filename = filename)

        whenever(generatedImageRepository.findByFilename(filename)).thenReturn(generatedImage)

        // When
        val result = imageQueryService.findImageByFilename(filename)

        // Then
        assertNotNull(result)
        assertEquals(filename, result!!.filename)
        assertEquals(ImageType.GENERATED, result.imageType)
        assertTrue(result is SimpleImageDto)
    }

    @Test
    fun `findImageByFilename should return null when generated image not found`() {
        // Given
        val filename = "non-existent-image.jpg"

        whenever(generatedImageRepository.findByFilename(filename)).thenReturn(null)

        // When
        val result = imageQueryService.findImageByFilename(filename)

        // Then
        assertNull(result)
    }

    // FIND UPLOADED IMAGE BY UUID TESTS

    @Test
    fun `findUploadedImageByUuid should return SimpleImageDto when uploaded image found`() {
        // Given
        val uuid = UUID.randomUUID()
        val storedFilename = "uploaded-image.jpg"
        val uploadedImage = createUploadedImage(uuid = uuid, storedFilename = storedFilename)

        whenever(uploadedImageRepository.findByUuid(uuid)).thenReturn(uploadedImage)

        // When
        val result = imageQueryService.findUploadedImageByUuid(uuid)

        // Then
        assertNotNull(result)
        assertEquals(storedFilename, result!!.filename)
        assertEquals(ImageType.PRIVATE, result.imageType)
        assertTrue(result is SimpleImageDto)
    }

    @Test
    fun `findUploadedImageByUuid should return null when uploaded image not found`() {
        // Given
        val uuid = UUID.randomUUID()

        whenever(uploadedImageRepository.findByUuid(uuid)).thenReturn(null)

        // When
        val result = imageQueryService.findUploadedImageByUuid(uuid)

        // Then
        assertNull(result)
    }

    // FIND UPLOADED IMAGES BY USER ID TESTS

    @Test
    fun `findUploadedImagesByUserId should return list of SimpleImageDto when images found`() {
        // Given
        val userId = 1L
        val uploadedImages =
            listOf(
                createUploadedImage(storedFilename = "image1.jpg", userId = userId),
                createUploadedImage(storedFilename = "image2.jpg", userId = userId),
                createUploadedImage(storedFilename = "image3.png", userId = userId),
            )

        whenever(uploadedImageRepository.findAllByUserId(userId)).thenReturn(uploadedImages)

        // When
        val result = imageQueryService.findUploadedImagesByUserId(userId)

        // Then
        assertEquals(3, result.size)
        assertEquals("image1.jpg", result[0].filename)
        assertEquals("image2.jpg", result[1].filename)
        assertEquals("image3.png", result[2].filename)
        result.forEach {
            assertEquals(ImageType.PRIVATE, it.imageType)
            assertTrue(it is SimpleImageDto)
        }
    }

    @Test
    fun `findUploadedImagesByUserId should return empty list when no images found`() {
        // Given
        val userId = 1L

        whenever(uploadedImageRepository.findAllByUserId(userId)).thenReturn(emptyList())

        // When
        val result = imageQueryService.findUploadedImagesByUserId(userId)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `findUploadedImagesByUserId should handle different users correctly`() {
        // Given
        val userId1 = 1L
        val userId2 = 2L
        val user1Images = listOf(createUploadedImage(storedFilename = "user1-image.jpg", userId = userId1))
        val user2Images =
            listOf(
                createUploadedImage(storedFilename = "user2-image1.jpg", userId = userId2),
                createUploadedImage(storedFilename = "user2-image2.jpg", userId = userId2),
            )

        whenever(uploadedImageRepository.findAllByUserId(userId1)).thenReturn(user1Images)
        whenever(uploadedImageRepository.findAllByUserId(userId2)).thenReturn(user2Images)

        // When
        val result1 = imageQueryService.findUploadedImagesByUserId(userId1)
        val result2 = imageQueryService.findUploadedImagesByUserId(userId2)

        // Then
        assertEquals(1, result1.size)
        assertEquals("user1-image.jpg", result1[0].filename)

        assertEquals(2, result2.size)
        assertEquals("user2-image1.jpg", result2[0].filename)
        assertEquals("user2-image2.jpg", result2[1].filename)
    }

    // EXISTS BY UUID TESTS

    @Test
    fun `existsByUuid should return true when uploaded image exists`() {
        // Given
        val uuid = UUID.randomUUID()
        val uploadedImage = createUploadedImage(uuid = uuid)

        whenever(uploadedImageRepository.findByUuid(uuid)).thenReturn(uploadedImage)

        // When
        val result = imageQueryService.existsByUuid(uuid)

        // Then
        assertTrue(result)
    }

    @Test
    fun `existsByUuid should return false when uploaded image does not exist`() {
        // Given
        val uuid = UUID.randomUUID()

        whenever(uploadedImageRepository.findByUuid(uuid)).thenReturn(null)

        // When
        val result = imageQueryService.existsByUuid(uuid)

        // Then
        assertFalse(result)
    }

    // EXISTS BY UUID AND USER ID TESTS

    @Test
    fun `existsByUuidAndUserId should return true when uploaded image exists for user`() {
        // Given
        val uuid = UUID.randomUUID()
        val userId = 1L
        val uploadedImage = createUploadedImage(uuid = uuid, userId = userId)

        whenever(uploadedImageRepository.findByUserIdAndUuid(userId, uuid)).thenReturn(uploadedImage)

        // When
        val result = imageQueryService.existsByUuidAndUserId(uuid, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `existsByUuidAndUserId should return false when uploaded image does not exist for user`() {
        // Given
        val uuid = UUID.randomUUID()
        val userId = 1L

        whenever(uploadedImageRepository.findByUserIdAndUuid(userId, uuid)).thenReturn(null)

        // When
        val result = imageQueryService.existsByUuidAndUserId(uuid, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `existsByUuidAndUserId should return false when image exists for different user`() {
        // Given
        val uuid = UUID.randomUUID()
        val ownerId = 1L
        val requesterId = 2L

        whenever(uploadedImageRepository.findByUserIdAndUuid(requesterId, uuid)).thenReturn(null)

        // When
        val result = imageQueryService.existsByUuidAndUserId(uuid, requesterId)

        // Then
        assertFalse(result)
    }

    // EXISTS GENERATED IMAGE BY ID TESTS

    @Test
    fun `existsGeneratedImageById should return true when generated image exists`() {
        // Given
        val imageId = 123L

        whenever(generatedImageRepository.existsById(imageId)).thenReturn(true)

        // When
        val result = imageQueryService.existsGeneratedImageById(imageId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `existsGeneratedImageById should return false when generated image does not exist`() {
        // Given
        val imageId = 123L

        whenever(generatedImageRepository.existsById(imageId)).thenReturn(false)

        // When
        val result = imageQueryService.existsGeneratedImageById(imageId)

        // Then
        assertFalse(result)
    }

    // EXISTS GENERATED IMAGE BY ID AND USER ID TESTS

    @Test
    fun `existsGeneratedImageByIdAndUserId should return true when generated image exists for user`() {
        // Given
        val imageId = 123L
        val userId = 1L

        whenever(generatedImageRepository.existsByIdAndUserId(imageId, userId)).thenReturn(true)

        // When
        val result = imageQueryService.existsGeneratedImageByIdAndUserId(imageId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `existsGeneratedImageByIdAndUserId should return false when generated image does not exist for user`() {
        // Given
        val imageId = 123L
        val userId = 1L

        whenever(generatedImageRepository.existsByIdAndUserId(imageId, userId)).thenReturn(false)

        // When
        val result = imageQueryService.existsGeneratedImageByIdAndUserId(imageId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `existsGeneratedImageByIdAndUserId should handle different users correctly`() {
        // Given
        val imageId = 123L
        val ownerId = 1L
        val requesterId = 2L

        whenever(generatedImageRepository.existsByIdAndUserId(imageId, ownerId)).thenReturn(true)
        whenever(generatedImageRepository.existsByIdAndUserId(imageId, requesterId)).thenReturn(false)

        // When
        val ownerResult = imageQueryService.existsGeneratedImageByIdAndUserId(imageId, ownerId)
        val requesterResult = imageQueryService.existsGeneratedImageByIdAndUserId(imageId, requesterId)

        // Then
        assertTrue(ownerResult)
        assertFalse(requesterResult)
    }

    // EDGE CASE TESTS

    @Test
    fun `methods should handle null values gracefully`() {
        // Given - repositories return null for all queries
        whenever(generatedImageRepository.findByFilename("")).thenReturn(null)
        whenever(uploadedImageRepository.findByUuid(UUID.randomUUID())).thenReturn(null)
        whenever(uploadedImageRepository.findAllByUserId(0L)).thenReturn(emptyList())

        // When & Then - should not throw exceptions
        assertNull(imageQueryService.findImageByFilename(""))
        assertNull(imageQueryService.findUploadedImageByUuid(UUID.randomUUID()))
        assertEquals(0, imageQueryService.findUploadedImagesByUserId(0L).size)
        assertFalse(imageQueryService.existsByUuid(UUID.randomUUID()))
        assertFalse(imageQueryService.existsByUuidAndUserId(UUID.randomUUID(), 0L))
    }

    // HELPER METHODS

    private fun createUploadedImage(
        id: Long = 1L,
        uuid: UUID = UUID.randomUUID(),
        originalFilename: String = "original-image.jpg",
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
        id: Long = 1L,
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
