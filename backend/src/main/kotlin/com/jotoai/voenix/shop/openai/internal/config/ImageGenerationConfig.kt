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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
internal class ImageGenerationConfig {
    @Bean
    fun httpClient(): HttpClient =
        HttpClient(CIO) {
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

    @Bean
    fun providerMap(
        openAIProvider: com.jotoai.voenix.shop.openai.internal.provider.OpenAIImageProvider,
        geminiProvider: com.jotoai.voenix.shop.openai.internal.provider.GeminiImageProvider,
        fluxProvider: com.jotoai.voenix.shop.openai.internal.provider.FluxImageProvider,
    ): Map<OpenAIImageService.AiProvider, com.jotoai.voenix.shop.openai.internal.provider.ImageGenerationProvider> =
        mapOf(
            OpenAIImageService.AiProvider.OPENAI to openAIProvider,
            OpenAIImageService.AiProvider.GOOGLE to geminiProvider,
            OpenAIImageService.AiProvider.FLUX to fluxProvider,
        )
}
