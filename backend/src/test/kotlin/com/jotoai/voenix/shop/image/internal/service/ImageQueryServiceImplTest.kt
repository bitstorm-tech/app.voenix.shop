package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.user.api.UserService
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
import java.util.Optional
import java.util.UUID

class ImageQueryServiceImplTest {
    private lateinit var generatedImageRepository: GeneratedImageRepository
    private lateinit var uploadedImageRepository: UploadedImageRepository
    private lateinit var imageQueryService: ImageManagementService

    @BeforeEach
    fun setUp() {
        generatedImageRepository = mock()
        uploadedImageRepository = mock()

        imageQueryService =
            ImageManagementService(
                imageStorageService = mock<ImageStorageService>(),
                uploadedImageRepository = uploadedImageRepository,
                generatedImageRepository = generatedImageRepository,
                imageValidationService = mock<ImageValidationService>(),
                openAIImageGenerationService = mock<OpenAIImageGenerationService>(),
                storagePathService = mock<StoragePathService>(),
                userService = mock<UserService>(),
                userImageStorageService = mock<UserImageStorageService>(),
                imageConversionService = mock<ImageConversionService>(),
            )
    }

    // Note: findImageByFilename is no longer part of the ImageQueryService API; tests removed.

    // FIND UPLOADED IMAGE BY UUID TESTS

    @Test
    fun `findUploadedImageByUuid should return SimpleImageDto when uploaded image found`() {
        // Given
        val uuid = UUID.randomUUID()
        val storedFilename = "uploaded-image.jpg"
        val uploadedImage = createUploadedImage(UploadedImageParams(uuid = uuid, storedFilename = storedFilename))

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
                createUploadedImage(UploadedImageParams(storedFilename = "image1.jpg", userId = userId)),
                createUploadedImage(UploadedImageParams(storedFilename = "image2.jpg", userId = userId)),
                createUploadedImage(UploadedImageParams(storedFilename = "image3.png", userId = userId)),
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
        val user1Images =
            listOf(
                createUploadedImage(
                    UploadedImageParams(
                        storedFilename = "user1-image.jpg",
                        userId = userId1,
                    ),
                ),
            )
        val user2Images =
            listOf(
                createUploadedImage(UploadedImageParams(storedFilename = "user2-image1.jpg", userId = userId2)),
                createUploadedImage(UploadedImageParams(storedFilename = "user2-image2.jpg", userId = userId2)),
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
        val uploadedImage = createUploadedImage(UploadedImageParams(uuid = uuid))

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
        val uploadedImage = createUploadedImage(UploadedImageParams(uuid = uuid, userId = userId))

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

    // VALIDATE GENERATED IMAGE OWNERSHIP TESTS

    @Test
    fun `validateGeneratedImageOwnership should return true for authenticated user who owns the image`() {
        // Given
        val imageId = 123L
        val userId = 1L

        whenever(generatedImageRepository.existsByIdAndUserId(imageId, userId)).thenReturn(true)

        // When
        val result = imageQueryService.validateGeneratedImageOwnership(imageId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `validateGeneratedImageOwnership should return false for authenticated user who does not own the image`() {
        // Given
        val imageId = 123L
        val userId = 1L

        whenever(generatedImageRepository.existsByIdAndUserId(imageId, userId)).thenReturn(false)

        // When
        val result = imageQueryService.validateGeneratedImageOwnership(imageId, userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `validateGeneratedImageOwnership should return true for anonymous user when image exists`() {
        // Given
        val imageId = 123L
        val userId = null

        whenever(generatedImageRepository.existsById(imageId)).thenReturn(true)

        // When
        val result = imageQueryService.validateGeneratedImageOwnership(imageId, userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `validateGeneratedImageOwnership should return false for anonymous user when image does not exist`() {
        // Given
        val imageId = 123L
        val userId = null

        whenever(generatedImageRepository.existsById(imageId)).thenReturn(false)

        // When
        val result = imageQueryService.validateGeneratedImageOwnership(imageId, userId)

        // Then
        assertFalse(result)
    }

    // FIND GENERATED IMAGE BY ID TESTS

    @Test
    fun `findGeneratedImageById should return GeneratedImageDto when image exists`() {
        // Given
        val imageId = 123L
        val generatedImage =
            createGeneratedImage(
                GeneratedImageParams(
                    id = imageId,
                    filename = "test-image.jpg",
                    promptId = 100L,
                    userId = 1L,
                    ipAddress = "192.168.1.1",
                ),
            )

        whenever(generatedImageRepository.findById(imageId)).thenReturn(Optional.of(generatedImage))

        // When
        val result = imageQueryService.findGeneratedImageById(imageId)

        // Then
        assertNotNull(result)
        assertEquals("test-image.jpg", result!!.filename)
        assertEquals(ImageType.GENERATED, result.imageType)
        assertEquals(100L, result.promptId)
        assertEquals(1L, result.userId)
        assertEquals("192.168.1.1", result.ipAddress)
        assertTrue(result is GeneratedImageDto)
    }

    @Test
    fun `findGeneratedImageById should return null when image does not exist`() {
        // Given
        val imageId = 123L

        whenever(generatedImageRepository.findById(imageId)).thenReturn(Optional.empty())

        // When
        val result = imageQueryService.findGeneratedImageById(imageId)

        // Then
        assertNull(result)
    }

    @Test
    fun `findGeneratedImageById should handle anonymous user images`() {
        // Given
        val imageId = 123L
        val generatedImage =
            createGeneratedImage(
                GeneratedImageParams(
                    id = imageId,
                    filename = "anonymous-image.jpg",
                    promptId = 100L,
                    userId = null,
                    ipAddress = "127.0.0.1",
                ),
            )

        whenever(generatedImageRepository.findById(imageId)).thenReturn(Optional.of(generatedImage))

        // When
        val result = imageQueryService.findGeneratedImageById(imageId)

        // Then
        assertNotNull(result)
        assertEquals("anonymous-image.jpg", result!!.filename)
        assertEquals(ImageType.GENERATED, result.imageType)
        assertEquals(100L, result.promptId)
        assertNull(result.userId)
        assertEquals("127.0.0.1", result.ipAddress)
    }

    // FIND GENERATED IMAGES BY IDS TESTS

    @Test
    fun `findGeneratedImagesByIds should return map of images when all IDs exist`() {
        // Given
        val imageId1 = 123L
        val imageId2 = 124L
        val imageId3 = 125L
        val ids = listOf(imageId1, imageId2, imageId3)

        val generatedImage1 =
            createGeneratedImage(
                GeneratedImageParams(
                    id = imageId1,
                    filename = "image1.jpg",
                    promptId = 100L,
                ),
            )
        val generatedImage2 =
            createGeneratedImage(
                GeneratedImageParams(
                    id = imageId2,
                    filename = "image2.jpg",
                    promptId = 101L,
                ),
            )
        val generatedImage3 =
            createGeneratedImage(
                GeneratedImageParams(
                    id = imageId3,
                    filename = "image3.jpg",
                    promptId = 102L,
                ),
            )

        val allImages = listOf(generatedImage1, generatedImage2, generatedImage3)

        whenever(generatedImageRepository.findAllById(ids)).thenReturn(allImages)

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(3, result.size)

        assertNotNull(result[imageId1])
        assertEquals("image1.jpg", result[imageId1]!!.filename)
        assertEquals(100L, result[imageId1]!!.promptId)

        assertNotNull(result[imageId2])
        assertEquals("image2.jpg", result[imageId2]!!.filename)
        assertEquals(101L, result[imageId2]!!.promptId)

        assertNotNull(result[imageId3])
        assertEquals("image3.jpg", result[imageId3]!!.filename)
        assertEquals(102L, result[imageId3]!!.promptId)

        result.values.forEach {
            assertEquals(ImageType.GENERATED, it.imageType)
            assertTrue(it is GeneratedImageDto)
        }
    }

    @Test
    fun `findGeneratedImagesByIds should return partial results when some IDs exist`() {
        // Given
        val existingId1 = 123L
        val existingId2 = 124L
        val missingId = 999L
        val ids = listOf(existingId1, existingId2, missingId)

        val generatedImage1 = createGeneratedImage(GeneratedImageParams(id = existingId1, filename = "image1.jpg"))
        val generatedImage2 = createGeneratedImage(GeneratedImageParams(id = existingId2, filename = "image2.jpg"))

        val foundImages = listOf(generatedImage1, generatedImage2)

        whenever(generatedImageRepository.findAllById(ids)).thenReturn(foundImages)

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.containsKey(existingId1))
        assertTrue(result.containsKey(existingId2))
        assertFalse(result.containsKey(missingId))

        assertEquals("image1.jpg", result[existingId1]!!.filename)
        assertEquals("image2.jpg", result[existingId2]!!.filename)
    }

    @Test
    fun `findGeneratedImagesByIds should return empty map when no IDs exist`() {
        // Given
        val ids = listOf(123L, 124L, 125L)

        whenever(generatedImageRepository.findAllById(ids)).thenReturn(emptyList())

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `findGeneratedImagesByIds should return empty map when input list is empty`() {
        // Given
        val ids = emptyList<Long>()

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `findGeneratedImagesByIds should handle single ID`() {
        // Given
        val imageId = 123L
        val ids = listOf(imageId)
        val generatedImage = createGeneratedImage(GeneratedImageParams(id = imageId, filename = "single-image.jpg"))

        whenever(generatedImageRepository.findAllById(ids)).thenReturn(listOf(generatedImage))

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.containsKey(imageId))
        assertEquals("single-image.jpg", result[imageId]!!.filename)
    }

    @Test
    fun `findGeneratedImagesByIds should handle mixed user and anonymous images`() {
        // Given
        val userImageId = 123L
        val anonymousImageId = 124L
        val ids = listOf(userImageId, anonymousImageId)

        val userImage =
            createGeneratedImage(
                GeneratedImageParams(
                    id = userImageId,
                    filename = "user-image.jpg",
                    userId = 1L,
                ),
            )
        val anonymousImage =
            createGeneratedImage(
                GeneratedImageParams(
                    id = anonymousImageId,
                    filename = "anon-image.jpg",
                    userId = null,
                ),
            )

        val allImages = listOf(userImage, anonymousImage)

        whenever(generatedImageRepository.findAllById(ids)).thenReturn(allImages)

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(2, result.size)
        assertEquals("user-image.jpg", result[userImageId]!!.filename)
        assertEquals(1L, result[userImageId]!!.userId)
        assertEquals("anon-image.jpg", result[anonymousImageId]!!.filename)
        assertNull(result[anonymousImageId]!!.userId)
    }

    // EDGE CASE TESTS

    @Test
    fun `methods should handle null values gracefully`() {
        // Given - repositories return null for all queries
        whenever(generatedImageRepository.findByFilename("")).thenReturn(null)
        whenever(uploadedImageRepository.findByUuid(UUID.randomUUID())).thenReturn(null)
        whenever(uploadedImageRepository.findAllByUserId(0L)).thenReturn(emptyList())

        // When & Then - should not throw exceptions
        assertNull(imageQueryService.findUploadedImageByUuid(UUID.randomUUID()))
        assertEquals(0, imageQueryService.findUploadedImagesByUserId(0L).size)
        assertFalse(imageQueryService.existsByUuid(UUID.randomUUID()))
        assertFalse(imageQueryService.existsByUuidAndUserId(UUID.randomUUID(), 0L))
    }

    // HELPER METHODS

    private data class UploadedImageParams(
        val id: Long = 1L,
        val uuid: UUID = UUID.randomUUID(),
        val originalFilename: String = "original-image.jpg",
        val storedFilename: String = "stored-image.jpg",
        val contentType: String = "image/jpeg",
        val fileSize: Long = 1024L,
        val userId: Long = 1L,
        val uploadedAt: LocalDateTime = LocalDateTime.now(),
    )

    private fun createUploadedImage(params: UploadedImageParams = UploadedImageParams()): UploadedImage =
        UploadedImage(
            id = params.id,
            uuid = params.uuid,
            originalFilename = params.originalFilename,
            storedFilename = params.storedFilename,
            contentType = params.contentType,
            fileSize = params.fileSize,
            userId = params.userId,
            uploadedAt = params.uploadedAt,
        )

    private data class GeneratedImageParams(
        val id: Long = 1L,
        val uuid: UUID = UUID.randomUUID(),
        val filename: String = "generated-image.jpg",
        val promptId: Long = 100L,
        val userId: Long? = 1L,
        val generatedAt: LocalDateTime = LocalDateTime.now(),
        val ipAddress: String? = "127.0.0.1",
    )

    private fun createGeneratedImage(params: GeneratedImageParams = GeneratedImageParams()): GeneratedImage =
        GeneratedImage(
            id = params.id,
            uuid = params.uuid,
            filename = params.filename,
            promptId = params.promptId,
            userId = params.userId,
            generatedAt = params.generatedAt,
            ipAddress = params.ipAddress,
        )
}
