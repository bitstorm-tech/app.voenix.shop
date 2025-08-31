package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageOperations
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Simple smoke test for ImageOperationsService to verify it implements ImageOperations properly
 */
class ImageOperationsServiceTest {
    @Test
    fun `ImageOperationsService should implement ImageOperations`() {
        // Given
        val fileStorageService: FileStorageService = mockk()
        val uploadedImageRepository: UploadedImageRepository = mockk()
        val generatedImageRepository: GeneratedImageRepository = mockk()
        val imageValidationService: ImageValidationService = mockk()
        val userImageStorageService: UserImageStorageService = mockk()

        // When
        val service = ImageOperationsService(
            fileStorageService,
            uploadedImageRepository,
            generatedImageRepository,
            imageValidationService,
            userImageStorageService,
        )

        // Then
        assertNotNull(service)
        assertTrue(service is ImageOperations)
    }
}
