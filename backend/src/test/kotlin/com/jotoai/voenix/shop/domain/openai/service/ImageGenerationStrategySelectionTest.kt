package com.jotoai.voenix.shop.domain.openai.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests to verify that the correct ImageGenerationStrategy implementation
 * is selected based on the app.test-mode configuration property.
 */
class ImageGenerationStrategySelectionTest {
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode=true"])
    class TestModeEnabledTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should select TestModeImageGenerationStrategy when testmode is true`() {
            // When
            val strategy = applicationContext.getBean(ImageGenerationStrategy::class.java)

            // Then
            assertTrue(strategy is TestModeImageGenerationStrategy) {
                "Expected TestModeImageGenerationStrategy but got ${strategy::class.simpleName}"
            }
        }

        @Test
        fun `should not have OpenAIImageGenerationStrategy bean when testmode is true`() {
            // When
            val hasOpenAIStrategy = applicationContext.getBeanNamesForType(OpenAIImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(!hasOpenAIStrategy) {
                "OpenAIImageGenerationStrategy should not be available when test-mode is true"
            }
        }

        @Test
        fun `should have TestModeImageGenerationStrategy bean when testmode is true`() {
            // When
            val hasTestModeStrategy = applicationContext.getBeanNamesForType(TestModeImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(hasTestModeStrategy) {
                "TestModeImageGenerationStrategy should be available when test-mode is true"
            }
        }
    }

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode=false"])
    class TestModeDisabledTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should select OpenAIImageGenerationStrategy when testmode is false`() {
            // When
            val strategy = applicationContext.getBean(ImageGenerationStrategy::class.java)

            // Then
            assertTrue(strategy is OpenAIImageGenerationStrategy) {
                "Expected OpenAIImageGenerationStrategy but got ${strategy::class.simpleName}"
            }
        }

        @Test
        fun `should have OpenAIImageGenerationStrategy bean when testmode is false`() {
            // When
            val hasOpenAIStrategy = applicationContext.getBeanNamesForType(OpenAIImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(hasOpenAIStrategy) {
                "OpenAIImageGenerationStrategy should be available when test-mode is false"
            }
        }

        @Test
        fun `should not have TestModeImageGenerationStrategy bean when testmode is false`() {
            // When
            val hasTestModeStrategy = applicationContext.getBeanNamesForType(TestModeImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(!hasTestModeStrategy) {
                "TestModeImageGenerationStrategy should not be available when test-mode is false"
            }
        }
    }

    // No test-mode property specified - should default to OpenAI strategy
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode="])
    class TestModeDefaultTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should default to OpenAIImageGenerationStrategy when testmode property is not specified`() {
            // When
            val strategy = applicationContext.getBean(ImageGenerationStrategy::class.java)

            // Then
            assertTrue(strategy is OpenAIImageGenerationStrategy) {
                "Expected OpenAIImageGenerationStrategy (default) but got ${strategy::class.simpleName}"
            }
        }

        @Test
        fun `should have OpenAIImageGenerationStrategy bean when testmode is not specified`() {
            // When
            val hasOpenAIStrategy = applicationContext.getBeanNamesForType(OpenAIImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(hasOpenAIStrategy) {
                "OpenAIImageGenerationStrategy should be available by default"
            }
        }

        @Test
        fun `should not have TestModeImageGenerationStrategy bean when testmode is not specified`() {
            // When
            val hasTestModeStrategy = applicationContext.getBeanNamesForType(TestModeImageGenerationStrategy::class.java).isNotEmpty()

            // Then
            assertTrue(!hasTestModeStrategy) {
                "TestModeImageGenerationStrategy should not be available by default"
            }
        }
    }

    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = ["app.test-mode=invalid-value"])
    class TestModeInvalidValueTest {
        @Autowired
        private lateinit var applicationContext: ApplicationContext

        @Test
        fun `should default to OpenAIImageGenerationStrategy when testmode has invalid value`() {
            // When
            val strategy = applicationContext.getBean(ImageGenerationStrategy::class.java)

            // Then
            assertTrue(strategy is OpenAIImageGenerationStrategy) {
                "Expected OpenAIImageGenerationStrategy (default for invalid value) but got ${strategy::class.simpleName}"
            }
        }
    }
}
