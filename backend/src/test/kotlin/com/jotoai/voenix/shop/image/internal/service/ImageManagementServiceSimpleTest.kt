package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
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
        val imageValidationService: ImageValidationService = mock()
        val openAIImageGenerationService: OpenAIImageGenerationService = mock()
        val storagePathService: StoragePathService = mock()
        val promptQueryService: PromptQueryService = mock()
        val userService: UserService = mock()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
                imageValidationService,
                openAIImageGenerationService,
                storagePathService,
                promptQueryService,
                userService,
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
        val imageValidationService: ImageValidationService = mock()
        val openAIImageGenerationService: OpenAIImageGenerationService = mock()
        val storagePathService: StoragePathService = mock()
        val promptQueryService: PromptQueryService = mock()
        val userService: UserService = mock()

        // When
        val service =
            ImageManagementService(
                imageStorageService,
                uploadedImageRepository,
                generatedImageRepository,
                imageValidationService,
                openAIImageGenerationService,
                storagePathService,
                promptQueryService,
                userService,
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
