package com.jotoai.voenix.shop.openai.internal.service

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests to verify that the OpenAI service behaves correctly
 * based on the app.test-mode configuration property.
 */
class OpenAIServiceConfigurationTest {
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode=true"])
    class TestModeEnabledTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should have OpenAIImageService bean when testmode is true`() {
            // When
            val service = applicationContext.getBean(OpenAIImageService::class.java)

            // Then
            assertNotNull(service) {
                "OpenAIImageService should be available when test-mode is true"
            }
        }
    }

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode=false", "OPENAI_API_KEY=test-key-for-testing"])
    class TestModeDisabledTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should have OpenAIImageService bean when testmode is false`() {
            // When
            val service = applicationContext.getBean(OpenAIImageService::class.java)

            // Then
            assertNotNull(service) {
                "OpenAIImageService should be available when test-mode is false"
            }
        }
    }
}
