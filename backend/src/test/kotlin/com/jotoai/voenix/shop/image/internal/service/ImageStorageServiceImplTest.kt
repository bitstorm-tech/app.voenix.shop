package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.image.internal.service.ImageValidationService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
        storagePathService = mock()
        imageConversionService = mock()
        uploadedImageRepository = mock()
        generatedImageRepository = mock()
        imageValidationService = mock()

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

        whenever(storagePathService.getPhysicalFilePath(imageType, filename)).thenReturn(mockPath)

        // When
        val result = imageStorageService.fileExists(filename, imageType)

        // Then - will return false since the file doesn't exist at the mock path
        assertTrue(!result) // File doesn't exist in test environment
        verify(storagePathService).getPhysicalFilePath(imageType, filename)
    }
}
