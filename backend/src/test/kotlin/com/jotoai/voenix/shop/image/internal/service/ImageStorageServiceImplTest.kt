package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile

class ImageStorageServiceImplTest {
    private lateinit var storagePathService: StoragePathService
    private lateinit var imageService: ImageService
    private lateinit var imageStorageService: ImageStorageServiceImpl

    private lateinit var mockFile: MultipartFile

    @BeforeEach
    fun setUp() {
        storagePathService = mock()
        imageService = mock()

        imageStorageService =
            ImageStorageServiceImpl(
                storagePathService = storagePathService,
                imageService = imageService,
            )

        mockFile =
            MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                "test content".toByteArray(),
            )
    }

    // STORE FILE (MULTIPART) TESTS

    @Test
    fun `storeFile with MultipartFile should throw UnsupportedOperationException`() {
        // When & Then
        val exception =
            assertThrows<UnsupportedOperationException> {
                imageStorageService.storeFile(mockFile, ImageType.PRIVATE)
            }

        assertEquals("Use ImageFacade.createImage instead", exception.message)

        // Verify no interactions with dependencies
        verifyNoInteractions(storagePathService)
        verifyNoInteractions(imageService)
    }

    @Test
    fun `storeFile with MultipartFile should throw for all ImageType values`() {
        // Test all ImageType enum values
        ImageType.values().forEach { imageType ->
            assertThrows<UnsupportedOperationException> {
                imageStorageService.storeFile(mockFile, imageType)
            }
        }
    }

    // STORE FILE (BYTE ARRAY) TESTS

    @Test
    fun `storeFile with ByteArray should throw UnsupportedOperationException`() {
        // Given
        val bytes = "test content".toByteArray()
        val originalFilename = "test-image.jpg"

        // When & Then
        val exception =
            assertThrows<UnsupportedOperationException> {
                imageStorageService.storeFile(bytes, originalFilename, ImageType.PRIVATE)
            }

        assertEquals("Not yet implemented", exception.message)

        // Verify no interactions with dependencies
        verifyNoInteractions(storagePathService)
        verifyNoInteractions(imageService)
    }

    @Test
    fun `storeFile with ByteArray should throw for all ImageType values`() {
        // Given
        val bytes = "test content".toByteArray()
        val originalFilename = "test-image.jpg"

        // Test all ImageType enum values
        ImageType.values().forEach { imageType ->
            assertThrows<UnsupportedOperationException> {
                imageStorageService.storeFile(bytes, originalFilename, imageType)
            }
        }
    }

    @Test
    fun `storeFile with ByteArray should handle empty byte array`() {
        // Given
        val emptyBytes = ByteArray(0)
        val originalFilename = "empty-image.jpg"

        // When & Then
        assertThrows<UnsupportedOperationException> {
            imageStorageService.storeFile(emptyBytes, originalFilename, ImageType.PUBLIC)
        }
    }

    @Test
    fun `storeFile with ByteArray should handle null filename`() {
        // Given
        val bytes = "test content".toByteArray()

        // When & Then
        assertThrows<UnsupportedOperationException> {
            imageStorageService.storeFile(bytes, "", ImageType.GENERATED)
        }
    }

    // LOAD FILE AS RESOURCE TESTS

    @Test
    fun `loadFileAsResource should throw UnsupportedOperationException`() {
        // Given
        val filename = "test-image.jpg"

        // When & Then
        val exception =
            assertThrows<UnsupportedOperationException> {
                imageStorageService.loadFileAsResource(filename, ImageType.PRIVATE)
            }

        assertEquals("Use ImageAccessService instead", exception.message)

        // Verify no interactions with dependencies
        verifyNoInteractions(storagePathService)
        verifyNoInteractions(imageService)
    }

    @Test
    fun `loadFileAsResource should throw for all ImageType values`() {
        // Given
        val filename = "test-image.jpg"

        // Test all ImageType enum values
        ImageType.values().forEach { imageType ->
            assertThrows<UnsupportedOperationException> {
                imageStorageService.loadFileAsResource(filename, imageType)
            }
        }
    }

    @Test
    fun `loadFileAsResource should handle empty filename`() {
        // When & Then
        assertThrows<UnsupportedOperationException> {
            imageStorageService.loadFileAsResource("", ImageType.PUBLIC)
        }
    }

    // GENERATE IMAGE URL TESTS

    @Test
    fun `generateImageUrl should delegate to StoragePathService`() {
        // Given
        val filename = "test-image.jpg"
        val imageType = ImageType.PRIVATE
        val expectedUrl = "https://example.com/images/private/test-image.jpg"

        whenever(storagePathService.getImageUrl(imageType, filename)).thenReturn(expectedUrl)

        // When
        val result = imageStorageService.generateImageUrl(filename, imageType)

        // Then
        assertEquals(expectedUrl, result)
        verify(storagePathService).getImageUrl(imageType, filename)
        verifyNoInteractions(imageService)
    }

    @Test
    fun `generateImageUrl should work for all ImageType values`() {
        // Given
        val filename = "test-image.jpg"
        val baseUrl = "https://example.com/images"

        ImageType.values().forEach { imageType ->
            val expectedUrl = "$baseUrl/${imageType.name.lowercase()}/$filename"
            whenever(storagePathService.getImageUrl(imageType, filename)).thenReturn(expectedUrl)

            // When
            val result = imageStorageService.generateImageUrl(filename, imageType)

            // Then
            assertEquals(expectedUrl, result)
            verify(storagePathService).getImageUrl(imageType, filename)
        }
    }

    @Test
    fun `generateImageUrl should handle empty filename`() {
        // Given
        val filename = ""
        val imageType = ImageType.PUBLIC
        val expectedUrl = "https://example.com/images/public/"

        whenever(storagePathService.getImageUrl(imageType, filename)).thenReturn(expectedUrl)

        // When
        val result = imageStorageService.generateImageUrl(filename, imageType)

        // Then
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `generateImageUrl should handle special characters in filename`() {
        // Given
        val filename = "test image with spaces & symbols.jpg"
        val imageType = ImageType.GENERATED
        val expectedUrl = "https://example.com/images/generated/test%20image%20with%20spaces%20%26%20symbols.jpg"

        whenever(storagePathService.getImageUrl(imageType, filename)).thenReturn(expectedUrl)

        // When
        val result = imageStorageService.generateImageUrl(filename, imageType)

        // Then
        assertEquals(expectedUrl, result)
    }

    // DELETE FILE TESTS

    @Test
    fun `deleteFile should delegate to ImageService and return true`() {
        // Given
        val filename = "test-image.jpg"
        val imageType = ImageType.PRIVATE

        // When
        val result = imageStorageService.deleteFile(filename, imageType)

        // Then
        assertTrue(result)
        verify(imageService).delete(filename)
        verifyNoInteractions(storagePathService)
    }

    @Test
    fun `deleteFile should work for all ImageType values`() {
        // Given
        val filename = "test-image.jpg"
        val results = mutableListOf<Boolean>()

        ImageType.values().forEach { imageType ->
            // When
            val result = imageStorageService.deleteFile(filename, imageType)
            results.add(result)
        }

        // Then
        results.forEach { assertTrue(it) }
        verify(imageService, times(ImageType.values().size)).delete(filename)
    }

    @Test
    fun `deleteFile should handle deletion exceptions gracefully`() {
        // Given
        val filename = "test-image.jpg"
        val imageType = ImageType.PUBLIC

        doThrow(RuntimeException("Delete failed")).`when`(imageService).delete(filename)

        // When & Then - exception should propagate
        assertThrows<RuntimeException> {
            imageStorageService.deleteFile(filename, imageType)
        }
    }

    @Test
    fun `deleteFile should handle empty filename`() {
        // Given
        val filename = ""
        val imageType = ImageType.GENERATED

        // When
        val result = imageStorageService.deleteFile(filename, imageType)

        // Then
        assertTrue(result)
        verify(imageService).delete(filename)
    }

    @Test
    fun `deleteFile should handle null filename gracefully`() {
        // Given - simulating what would happen if filename were null (though method signature prevents this)
        val filename = "null-filename"
        val imageType = ImageType.PRIVATE

        // When
        val result = imageStorageService.deleteFile(filename, imageType)

        // Then
        assertTrue(result)
        verify(imageService).delete(filename)
    }

    // FILE EXISTS TESTS

    @Test
    fun `fileExists should throw UnsupportedOperationException`() {
        // Given
        val filename = "test-image.jpg"

        // When & Then
        val exception =
            assertThrows<UnsupportedOperationException> {
                imageStorageService.fileExists(filename, ImageType.PRIVATE)
            }

        assertEquals("Not yet implemented", exception.message)

        // Verify no interactions with dependencies
        verifyNoInteractions(storagePathService)
        verifyNoInteractions(imageService)
    }

    @Test
    fun `fileExists should throw for all ImageType values`() {
        // Given
        val filename = "test-image.jpg"

        // Test all ImageType enum values
        ImageType.values().forEach { imageType ->
            assertThrows<UnsupportedOperationException> {
                imageStorageService.fileExists(filename, imageType)
            }
        }
    }

    @Test
    fun `fileExists should handle empty filename`() {
        // When & Then
        assertThrows<UnsupportedOperationException> {
            imageStorageService.fileExists("", ImageType.PUBLIC)
        }
    }

    // INTEGRATION TESTS

    @Test
    fun `service should handle mixed operations correctly`() {
        // Given
        val filename = "test-image.jpg"
        val imageType = ImageType.PRIVATE
        val expectedUrl = "https://example.com/images/private/test-image.jpg"

        whenever(storagePathService.getImageUrl(imageType, filename)).thenReturn(expectedUrl)

        // When - perform supported operations
        val url = imageStorageService.generateImageUrl(filename, imageType)
        val deleteResult = imageStorageService.deleteFile(filename, imageType)

        // Then
        assertEquals(expectedUrl, url)
        assertTrue(deleteResult)

        // When - perform unsupported operations
        assertThrows<UnsupportedOperationException> {
            imageStorageService.storeFile(mockFile, imageType)
        }

        assertThrows<UnsupportedOperationException> {
            imageStorageService.loadFileAsResource(filename, imageType)
        }

        assertThrows<UnsupportedOperationException> {
            imageStorageService.fileExists(filename, imageType)
        }
    }
}
