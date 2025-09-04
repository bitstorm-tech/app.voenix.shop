package com.jotoai.voenix.shop.image.internal.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import java.nio.file.Files

@Configuration
class StorageInitializer(
    private val storagePathConfiguration: StoragePathConfiguration,
) {
    private val logger = KotlinLogging.logger {}

    @Bean
    fun imageStorageInitializer(): ApplicationRunner =
        ApplicationRunner {
            storagePathConfiguration.pathMappings.forEach { (imageType, pathConfig) ->
                val physicalPath = storagePathConfiguration.storageRoot.resolve(pathConfig.relativePath)
                try {
                    Files.createDirectories(physicalPath)
                    logger.debug { "Ensured storage directory exists for $imageType: ${physicalPath.toAbsolutePath()}" }
                } catch (e: IOException) {
                    logger.error(
                        e,
                    ) { "Failed to create storage directory for $imageType at ${physicalPath.toAbsolutePath()}: ${e.message}" }
                }
            }
        }
}
