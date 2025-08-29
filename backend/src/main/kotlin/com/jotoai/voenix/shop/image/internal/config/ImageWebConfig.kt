package com.jotoai.voenix.shop.image.internal.config

import com.jotoai.voenix.shop.image.api.StoragePathService
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ImageWebConfig(
    private val storagePathService: StoragePathService,
) : WebMvcConfigurer {
    companion object {
        private const val CACHE_PERIOD_SECONDS = 3600
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Configure resource handlers for publicly accessible image types
        storagePathService
            .getAllConfiguredImageTypes()
            .filter { storagePathService.isPubliclyAccessible(it) }
            .forEach { imageType ->
                val urlPath = storagePathService.getUrlPath(imageType)
                val physicalPath = storagePathService.getPhysicalPath(imageType)

                registry
                    .addResourceHandler("$urlPath/**")
                    .addResourceLocations("file:${physicalPath.toAbsolutePath()}/")
                    .setCachePeriod(CACHE_PERIOD_SECONDS)
            }
    }
}
