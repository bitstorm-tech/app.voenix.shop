package com.jotoai.voenix.shop.common.config

import com.jotoai.voenix.shop.image.api.StoragePathService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.resource.PathResourceResolver

@Configuration
class WebConfig(
    @param:Value("\${spring.profiles.active:default}") private val activeProfile: String,
    private val storagePathService: StoragePathService,
) : WebMvcConfigurer {
    companion object {
        private const val CORS_MAX_AGE_SECONDS = 3600L
        private const val CACHE_PERIOD_SECONDS = 3600
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        // Only enable CORS in development
        if (activeProfile != "prod") {
            registry
                .addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(CORS_MAX_AGE_SECONDS)
        }
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

        // Serve static resources
        registry
            .addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .resourceChain(true)
            .addResolver(
                object : PathResourceResolver() {
                    override fun getResource(
                        resourcePath: String,
                        location: Resource,
                    ): Resource? {
                        val requestedResource = location.createRelative(resourcePath)

                        // If the requested resource exists and is readable, return it
                        return if (requestedResource.exists() && requestedResource.isReadable) {
                            requestedResource
                        } else {
                            // Don't try to serve index.html for images or API routes
                            if (!resourcePath.startsWith("api/") &&
                                !resourcePath.endsWith(".png") &&
                                !resourcePath.endsWith(".jpg") &&
                                !resourcePath.endsWith(".jpeg") &&
                                !resourcePath.endsWith(".gif") &&
                                !resourcePath.endsWith(".webp")
                            ) {
                                // Only return index.html if it exists
                                val indexResource = ClassPathResource("/static/index.html")
                                if (indexResource.exists()) {
                                    indexResource
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                    }
                },
            )
    }
}
