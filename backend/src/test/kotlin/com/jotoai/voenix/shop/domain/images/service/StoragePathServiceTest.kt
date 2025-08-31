package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.config.ImageTypePathConfig
import com.jotoai.voenix.shop.image.internal.config.StoragePathConfiguration
import com.jotoai.voenix.shop.image.internal.service.StoragePathServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for StoragePathService to verify centralized path management functionality.
 */
class StoragePathServiceTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var storagePathService: StoragePathServiceImpl

    @BeforeEach
    fun setUp() {
        val pathMappings =
            mapOf(
                ImageType.PRIVATE to
                    ImageTypePathConfig(
                        relativePath = "private/images",
                        urlPath = "/api/user/images",
                        isPubliclyAccessible = false,
                    ),
                ImageType.PUBLIC to
                    ImageTypePathConfig(
                        relativePath = "public/images",
                        urlPath = "/api/public/images",
                        isPubliclyAccessible = false,
                    ),
                ImageType.PROMPT_EXAMPLE to
                    ImageTypePathConfig(
                        relativePath = "public/images/prompt-example-images",
                        urlPath = "/images/prompt-example-images",
                        isPubliclyAccessible = true,
                    ),
            )

        val configuration =
            StoragePathConfiguration(
                storageRoot = tempDir,
                pathMappings = pathMappings,
            )

        storagePathService = StoragePathServiceImpl(configuration)
    }

    @Test
    fun `should return correct physical path for image type`() {
        val physicalPath = storagePathService.getPhysicalPath(ImageType.PRIVATE)
        assertEquals(tempDir.resolve("private/images"), physicalPath)
    }

    @Test
    fun `should return correct physical file path`() {
        val filename = "test-image.jpg"
        val physicalFilePath = storagePathService.getPhysicalFilePath(ImageType.PRIVATE, filename)
        assertEquals(tempDir.resolve("private/images/test-image.jpg"), physicalFilePath)
    }

    @Test
    fun `should return correct URL path for image type`() {
        val urlPath = storagePathService.getUrlPath(ImageType.PRIVATE)
        assertEquals("/api/user/images", urlPath)
    }

    @Test
    fun `should return correct image URL`() {
        val filename = "test-image.jpg"
        val imageUrl = storagePathService.getImageUrl(ImageType.PRIVATE, filename)
        assertEquals("/api/user/images/test-image.jpg", imageUrl)
    }

    @Test
    fun `should handle URL path ending with slash`() {
        // Create a path config with trailing slash
        val pathMappings =
            mapOf(
                ImageType.PRIVATE to
                    ImageTypePathConfig(
                        relativePath = "private/images",
                        urlPath = "/api/user/images/",
                        isPubliclyAccessible = false,
                    ),
            )

        val configuration =
            StoragePathConfiguration(
                storageRoot = tempDir,
                pathMappings = pathMappings,
            )

        val service = StoragePathServiceImpl(configuration)
        val filename = "test-image.jpg"
        val imageUrl = service.getImageUrl(ImageType.PRIVATE, filename)
        assertEquals("/api/user/images/test-image.jpg", imageUrl)
    }

    @Test
    fun `should correctly identify publicly accessible image types`() {
        assertTrue(storagePathService.isPubliclyAccessible(ImageType.PROMPT_EXAMPLE))
        assertFalse(storagePathService.isPubliclyAccessible(ImageType.PRIVATE))
        assertFalse(storagePathService.isPubliclyAccessible(ImageType.PUBLIC))
    }

    @Test
    fun `should return all configured image types`() {
        val configuredTypes = storagePathService.getAllConfiguredImageTypes()
        assertEquals(3, configuredTypes.size)
        assertTrue(configuredTypes.contains(ImageType.PRIVATE))
        assertTrue(configuredTypes.contains(ImageType.PUBLIC))
        assertTrue(configuredTypes.contains(ImageType.PROMPT_EXAMPLE))
    }

    @Test
    fun `should find image type by filename when file exists`() {
        val filename = "test-image.jpg"
        val physicalPath = storagePathService.getPhysicalPath(ImageType.PRIVATE)
        Files.createDirectories(physicalPath)
        Files.createFile(physicalPath.resolve(filename))

        val foundType = storagePathService.findImageTypeByFilename(filename)
        assertEquals(ImageType.PRIVATE, foundType)
    }

    @Test
    fun `should return null when filename not found in any directory`() {
        val filename = "non-existent-image.jpg"
        val foundType = storagePathService.findImageTypeByFilename(filename)
        assertNull(foundType)
    }

    @Test
    fun `should return storage root`() {
        val storageRoot = storagePathService.getStorageRoot()
        assertEquals(tempDir, storageRoot)
    }

    @Test
    fun `should create directories on initialization`() {
        // Directories should be created during service initialization
        assertTrue(Files.exists(tempDir.resolve("private/images")))
        assertTrue(Files.exists(tempDir.resolve("public/images")))
        assertTrue(Files.exists(tempDir.resolve("public/images/prompt-example-images")))
    }

    @Test
    fun `should throw exception for unknown image type`() {
        val pathMappings =
            mapOf(
                ImageType.PRIVATE to
                    ImageTypePathConfig(
                        relativePath = "private/images",
                        urlPath = "/api/user/images",
                        isPubliclyAccessible = false,
                    ),
            )

        val configuration =
            StoragePathConfiguration(
                storageRoot = tempDir,
                pathMappings = pathMappings,
            )

        val service = StoragePathServiceImpl(configuration)

        try {
            service.getPhysicalPath(ImageType.PUBLIC)
            assert(false) { "Expected IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertNotNull(e.message)
            assertTrue(e.message!!.contains("No path configuration found for ImageType: PUBLIC"))
        }
    }
}
