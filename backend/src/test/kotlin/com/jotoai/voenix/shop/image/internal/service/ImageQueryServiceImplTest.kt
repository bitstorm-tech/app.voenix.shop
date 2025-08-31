package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class ImageQueryServiceImplTest {
    private lateinit var generatedImageRepository: GeneratedImageRepository
    private lateinit var imageQueryService: ImageQueryServiceImpl

    @BeforeEach
    fun setUp() {
        generatedImageRepository = mockk()

        imageQueryService =
            ImageQueryServiceImpl(
                generatedImageRepository = generatedImageRepository,
            )
    }

    // EXISTS GENERATED IMAGE BY ID TESTS

    @Test
    fun `existsGeneratedImageById should return true when generated image exists`() {
        // Given
        val imageId = 123L

        every { generatedImageRepository.existsById(imageId) } returns true

        // When
        val result = imageQueryService.existsGeneratedImageById(imageId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `existsGeneratedImageById should return false when generated image does not exist`() {
        // Given
        val imageId = 123L

        every { generatedImageRepository.existsById(imageId) } returns false

        // When
        val result = imageQueryService.existsGeneratedImageById(imageId)

        // Then
        assertFalse(result)
    }

    // VALIDATE GENERATED IMAGE OWNERSHIP TESTS

    @Test
    fun `validateGeneratedImageOwnership should return true for authenticated user who owns the image`() {
        // Given
        val imageId = 123L
        val userId = 1L

        every { generatedImageRepository.existsByIdAndUserId(imageId, userId) } returns true

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

        every { generatedImageRepository.existsByIdAndUserId(imageId, userId) } returns false

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

        every { generatedImageRepository.existsById(imageId) } returns true

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

        every { generatedImageRepository.existsById(imageId) } returns false

        // When
        val result = imageQueryService.validateGeneratedImageOwnership(imageId, userId)

        // Then
        assertFalse(result)
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

        every { generatedImageRepository.findAllById(ids) } returns allImages

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

        every { generatedImageRepository.findAllById(ids) } returns foundImages

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

        every { generatedImageRepository.findAllById(ids) } returns emptyList()

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

        every { generatedImageRepository.findAllById(ids) } returns listOf(generatedImage)

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

        every { generatedImageRepository.findAllById(ids) } returns allImages

        // When
        val result = imageQueryService.findGeneratedImagesByIds(ids)

        // Then
        assertEquals(2, result.size)
        assertEquals("user-image.jpg", result[userImageId]!!.filename)
        assertEquals(1L, result[userImageId]!!.userId)
        assertEquals("anon-image.jpg", result[anonymousImageId]!!.filename)
        assertNull(result[anonymousImageId]!!.userId)
    }

    // HELPER METHODS

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