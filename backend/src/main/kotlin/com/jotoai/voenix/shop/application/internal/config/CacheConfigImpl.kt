package com.jotoai.voenix.shop.application.internal.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableCaching
@EnableScheduling
class CacheConfigImpl {
    @Bean
    fun cacheManager(): CacheManager =
        ConcurrentMapCacheManager(
            "publicPrompts",
            "uploadedImages",
            "generatedImages",
            "userUploadedImages",
            "userGeneratedImages",
        )

    @Scheduled(fixedDelay = 300000) // 5 minutes in milliseconds
    @CacheEvict(value = ["publicPrompts"], allEntries = true)
    fun evictPublicPromptsCache() {
        // This method will be called every 5 minutes to clear the cache
    }
}
