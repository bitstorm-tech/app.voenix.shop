package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.test.util.ReflectionTestUtils

/**
 * Simple smoke test for ImageManagementService to verify it implements ImageFacade properly
 */
class ImageManagementServiceSimpleTest {
    @Test
    fun `ImageManagementService should implement ImageFacade`() {
        // Given
        val imageStorageService: ImageStorageService = mock()
        val uploadedImageRepository: UploadedImageRepository = mock()
        val generatedImageRepository: GeneratedImageRepository = mock()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
            )

        // Then
        assertNotNull(service)
        assertTrue(service is ImageFacade, "ImageManagementService should implement ImageFacade")
    }

    @Test
    fun `ImageManagementService should be properly initialized`() {
        // Given
        val imageStorageService: ImageStorageService = mock()
        val uploadedImageRepository: UploadedImageRepository = mock()
        val generatedImageRepository: GeneratedImageRepository = mock()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
            )

        // Then
        val storageService = ReflectionTestUtils.getField(service, "imageStorageService")
        val uploadedRepo = ReflectionTestUtils.getField(service, "uploadedImageRepository")
        val generatedRepo = ReflectionTestUtils.getField(service, "generatedImageRepository")

        assertNotNull(storageService)
        assertNotNull(uploadedRepo)
        assertNotNull(generatedRepo)
    }
}
