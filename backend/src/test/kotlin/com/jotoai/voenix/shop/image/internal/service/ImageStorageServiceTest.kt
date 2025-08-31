package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Simple smoke test for ImageStorageService to verify it implements ImageStorage properly
 */
class ImageStorageServiceTest {
    @Test
    fun `ImageStorageService should implement ImageStorage`() {
        // Given
        val fileStorageService: FileStorageService = mockk()
        val storagePathServiceImpl: StoragePathServiceImpl = mockk()
        val uploadedImageRepository: UploadedImageRepository = mockk()
        val generatedImageRepository: GeneratedImageRepository = mockk()
        val userImageStorageService: UserImageStorageService = mockk()

        // When
        val service = ImageStorageService(
            fileStorageService,
            storagePathServiceImpl,
            uploadedImageRepository,
            generatedImageRepository,
            userImageStorageService,
        )

        // Then
        assertNotNull(service)
        assertTrue(service is ImageStorage)
    }
}
