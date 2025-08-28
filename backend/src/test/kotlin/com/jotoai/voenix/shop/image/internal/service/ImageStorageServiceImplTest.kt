package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.image.internal.service.ImageValidationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Paths

class ImageStorageServiceImplTest {
    private lateinit var storagePathService: StoragePathService
    private lateinit var imageConversionService: ImageConversionService
    private lateinit var uploadedImageRepository: UploadedImageRepository
    private lateinit var generatedImageRepository: GeneratedImageRepository
    private lateinit var imageValidationService: ImageValidationService
    private lateinit var imageStorageService: ImageStorageServiceImpl

    private lateinit var mockFile: MultipartFile

    @BeforeEach
    fun setUp() {
        storagePathService = mockk()
        imageConversionService = mockk()
        uploadedImageRepository = mockk()
        generatedImageRepository = mockk()
        imageValidationService = mockk()

        imageStorageService =
            ImageStorageServiceImpl(
                storagePathService = storagePathService,
                imageConversionService = imageConversionService,
                uploadedImageRepository = uploadedImageRepository,
                generatedImageRepository = generatedImageRepository,
                imageValidationService = imageValidationService,
            )

        mockFile =
            MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test content".toByteArray(),
            )
    }

    @Test
    fun `fileExists should check file existence via StoragePathService`() {
        // Given
        val filename = "test-image.jpg"
        val imageType = ImageType.PUBLIC
        val mockPath = Paths.get("/tmp/test-image.jpg")

        every { storagePathService.getPhysicalFilePath(imageType, filename) } returns mockPath

        // When
        val result = imageStorageService.fileExists(filename, imageType)

        // Then - will return false since the file doesn't exist at the mock path
        assertTrue(!result) // File doesn't exist in test environment
        verify { storagePathService.getPhysicalFilePath(imageType, filename) }
    }
}
