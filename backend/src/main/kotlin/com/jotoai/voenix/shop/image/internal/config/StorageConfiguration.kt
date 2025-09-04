package com.jotoai.voenix.shop.image.internal.config

import com.jotoai.voenix.shop.image.ImageType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Spring configuration class that defines the storage path configuration for all image types.
 * This centralizes all path definitions and makes them easily configurable and extensible.
 */
@Configuration
class StorageConfiguration {
    /**
     * Creates the StoragePathConfiguration bean with all image type path mappings.
     *
     * @param storageRoot The root directory for file storage, configured via storage.root property
     * @return Configured StoragePathConfiguration instance
     */
    @Bean
    fun storagePathConfiguration(
        @Value($$"${storage.root:storage}") storageRoot: String,
    ): StoragePathConfiguration {
        val rootPath: Path = Paths.get(storageRoot).toAbsolutePath()

        val pathMappings =
            mapOf(
                // Private images - accessed via API with authentication
                ImageType.PRIVATE to
                    ImageTypePathConfig(
                        relativePath = "private/images",
                        urlPath = "/api/user/images",
                        isPubliclyAccessible = false,
                    ),
                // Prompt test images - accessed via admin API (not public)
                ImageType.PROMPT_TEST to
                    ImageTypePathConfig(
                        relativePath = "private/images/_prompt-test",
                        urlPath = "/api/admin/images/prompt-test",
                        isPubliclyAccessible = false,
                    ),
                // Public images - accessed via API (though they could be made publicly accessible)
                ImageType.PUBLIC to
                    ImageTypePathConfig(
                        relativePath = "public/images",
                        urlPath = "/images/public",
                        isPubliclyAccessible = true,
                    ),
                // Prompt example images - publicly accessible via static URL mapping
                ImageType.PROMPT_EXAMPLE to
                    ImageTypePathConfig(
                        relativePath = "public/images/prompt-example-images",
                        urlPath = "/images/prompt-example-images",
                        isPubliclyAccessible = true,
                    ),
                // Prompt slot variant example images - publicly accessible via static URL mapping
                ImageType.PROMPT_SLOT_VARIANT_EXAMPLE to
                    ImageTypePathConfig(
                        relativePath = "public/images/prompt-slot-variant-example-images",
                        urlPath = "/images/prompt-slot-variant-example-images",
                        isPubliclyAccessible = true,
                    ),
                // Mug variant example images - publicly accessible via static URL mapping
                ImageType.MUG_VARIANT_EXAMPLE to
                    ImageTypePathConfig(
                        relativePath = "public/images/articles/mugs/variant-example-images",
                        urlPath = "/images/articles/mugs/variant-example-images",
                        isPubliclyAccessible = true,
                    ),
                // Shirt variant example images - publicly accessible via static URL mapping
                ImageType.SHIRT_VARIANT_EXAMPLE to
                    ImageTypePathConfig(
                        relativePath = "public/images/articles/shirts/variant-example-images",
                        urlPath = "/images/articles/shirts/variant-example-images",
                        isPubliclyAccessible = true,
                    ),
            )

        return StoragePathConfiguration(
            storageRoot = rootPath,
            pathMappings = pathMappings,
        )
    }
}
