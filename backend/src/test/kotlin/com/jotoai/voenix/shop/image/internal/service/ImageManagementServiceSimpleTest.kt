package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

/**
 * Simple smoke test for ImageManagementService to verify it implements ImageFacade properly
 */
class ImageManagementServiceSimpleTest {
    @Test
    fun `ImageManagementService should implement ImageFacade`() {
        // Given
        val imageStorageService: ImageStorageService = mockk()
        val uploadedImageRepository: UploadedImageRepository = mockk()
        val generatedImageRepository: GeneratedImageRepository = mockk()
        val imageValidationService: ImageValidationService = mockk()
        val storagePathService: StoragePathService = mockk()
        val userImageStorageService: UserImageStorageService = mockk()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
                imageValidationService,
                storagePathService,
                userImageStorageService,
            )

        // Then
        assertNotNull(service)
        assertTrue(service is ImageFacade, "ImageManagementService should implement ImageFacade")
    }

    @Test
    fun `ImageManagementService should be properly initialized`() {
        // Given
        val imageStorageService: ImageStorageService = mockk()
        val uploadedImageRepository: UploadedImageRepository = mockk()
        val generatedImageRepository: GeneratedImageRepository = mockk()
        val imageValidationService: ImageValidationService = mockk()
        val storagePathService: StoragePathService = mockk()
        val userImageStorageService: UserImageStorageService = mockk()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
                imageValidationService,
                storagePathService,
                userImageStorageService,
            )

        // Then
        assertNotNull(service)

        // Verify all dependencies are properly injected
        val retrievedImageStorageService = ReflectionTestUtils.getField(service, "imageStorageService")
        val retrievedUploadedImageRepository = ReflectionTestUtils.getField(service, "uploadedImageRepository")
        val retrievedGeneratedImageRepository = ReflectionTestUtils.getField(service, "generatedImageRepository")
        val retrievedImageValidationService = ReflectionTestUtils.getField(service, "imageValidationService")

        assertNotNull(retrievedImageStorageService)
        assertNotNull(retrievedUploadedImageRepository)
        assertNotNull(retrievedGeneratedImageRepository)
        assertNotNull(retrievedImageValidationService)
    }
}
