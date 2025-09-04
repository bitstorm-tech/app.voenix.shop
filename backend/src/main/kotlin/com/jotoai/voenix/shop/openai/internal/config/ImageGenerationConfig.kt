package com.jotoai.voenix.shop.openai.internal.config

import com.jotoai.voenix.shop.openai.internal.service.OpenAIImageService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.jackson.jackson
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
internal class ImageGenerationConfig {

    @Bean
    fun httpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.INFO
            }
            engine {
                requestTimeout = Duration.ofMinutes(5).toMillis()
            }
        }
    }

    @Bean
    fun providerMap(
        openAIProvider: com.jotoai.voenix.shop.openai.internal.provider.OpenAIImageProvider,
        geminiProvider: com.jotoai.voenix.shop.openai.internal.provider.GeminiImageProvider,
        fluxProvider: com.jotoai.voenix.shop.openai.internal.provider.FluxImageProvider
    ): Map<OpenAIImageService.AiProvider, com.jotoai.voenix.shop.openai.internal.provider.ImageGenerationProvider> {
        return mapOf(
            OpenAIImageService.AiProvider.OPENAI to openAIProvider,
            OpenAIImageService.AiProvider.GOOGLE to geminiProvider,
            OpenAIImageService.AiProvider.FLUX to fluxProvider
        )
    }
}

@ConfigurationProperties(prefix = "app.image-generation")
data class ImageGenerationProperties(
    val timeout: Duration = Duration.ofMinutes(5),
    val publicRateLimit: RateLimitConfig = RateLimitConfig(10, Duration.ofHours(1)),
    val userRateLimit: RateLimitConfig = RateLimitConfig(50, Duration.ofHours(24)),
    val defaultImageCount: Int = 4,
    val models: Map<String, String> = mapOf(
        "OPENAI" to "gpt-image-1",
        "GOOGLE" to "gemini-2.5-flash-image-preview",
        "FLUX" to "flux-1"
    )
)

data class RateLimitConfig(
    val maxRequests: Int,
    val period: Duration
)