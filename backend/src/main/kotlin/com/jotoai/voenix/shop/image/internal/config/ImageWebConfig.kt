package com.jotoai.voenix.shop.image.internal.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ImageWebConfig(
    private val storagePathConfiguration: StoragePathConfiguration,
) : WebMvcConfigurer {
    companion object {
        private const val CACHE_PERIOD_SECONDS = 3600
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Configure resource handlers for publicly accessible image types
        storagePathConfiguration.pathMappings
            .filter { (_, pathConfig) -> pathConfig.isPubliclyAccessible }
            .forEach { (imageType, pathConfig) ->
                val urlPath = pathConfig.urlPath
                val physicalPath = storagePathConfiguration.storageRoot.resolve(pathConfig.relativePath)

                registry
                    .addResourceHandler("$urlPath/**")
                    .addResourceLocations("file:${physicalPath.toAbsolutePath()}/")
                    .setCachePeriod(CACHE_PERIOD_SECONDS)
            }
    }
}
