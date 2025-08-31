package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.internal.config.StoragePathConfiguration
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Simple smoke test for ImageServiceImpl to verify it implements ImageService properly
 */
class ImageServiceImplTest {
    @Test
    fun `ImageServiceImpl should implement ImageService`() {
        // Given
        val imageOperationsService: ImageOperationsService = mockk()
        val generatedImageRepository: GeneratedImageRepository = mockk()
        val fileStorageService: FileStorageService = mockk()
        val storagePathConfiguration: StoragePathConfiguration = mockk()

        // When
        val service =
            ImageServiceImpl(
                imageOperationsService,
                generatedImageRepository,
                fileStorageService,
                storagePathConfiguration,
            )

        // Then
        assertNotNull(service)
        assertTrue(service is ImageService)
    }
}
