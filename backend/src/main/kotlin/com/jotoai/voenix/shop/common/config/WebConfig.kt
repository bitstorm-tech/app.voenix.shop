package com.jotoai.voenix.shop.common.config

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
    @Value("\${spring.profiles.active:default}") private val activeProfile: String,
    @Value("\${images.storage.root:storage}") private val storageRoot: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        // Only enable CORS in development
        if (activeProfile != "prod") {
            registry
                .addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600)
        }
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve public images
        registry
            .addResourceHandler("/images/public/**")
            .addResourceLocations("file:$storageRoot/images/public/")
            .setCachePeriod(3600)

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
                            // For SPA routing, return index.html for non-API routes
                            if (!resourcePath.startsWith("api/")) {
                                ClassPathResource("/static/index.html")
                            } else {
                                null
                            }
                        }
                    }
                },
            )
    }
}
