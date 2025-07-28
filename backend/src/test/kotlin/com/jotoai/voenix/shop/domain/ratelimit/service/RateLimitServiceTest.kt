package com.jotoai.voenix.shop.domain.ratelimit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimitServiceTest {
    private lateinit var rateLimitService: RateLimitService

    @BeforeEach
    fun setUp() {
        rateLimitService = RateLimitService()
    }

    @Test
    fun `should allow requests within rate limit`() {
        val identifier = "test-identifier"

        // First 5 requests should succeed
        for (i in 1..5) {
            assertTrue(rateLimitService.checkRateLimit(identifier))
            assertEquals(5 - i, rateLimitService.getRemainingAttempts(identifier))
        }
    }

    @Test
    fun `should block requests exceeding rate limit`() {
        val identifier = "test-identifier"

        // Use up all attempts
        repeat(5) {
            assertTrue(rateLimitService.checkRateLimit(identifier))
        }

        // 6th request should fail
        assertFalse(rateLimitService.checkRateLimit(identifier))
        assertEquals(0, rateLimitService.getRemainingAttempts(identifier))
    }

    @Test
    fun `should track different identifiers separately`() {
        val identifier1 = "test-identifier-1"
        val identifier2 = "test-identifier-2"

        // Use up all attempts for identifier1
        repeat(5) {
            assertTrue(rateLimitService.checkRateLimit(identifier1))
        }
        assertFalse(rateLimitService.checkRateLimit(identifier1))

        // identifier2 should still have all attempts available
        assertTrue(rateLimitService.checkRateLimit(identifier2))
        assertEquals(4, rateLimitService.getRemainingAttempts(identifier2))
    }

    @Test
    fun `should generate unique session tokens`() {
        val tokens = mutableSetOf<String>()

        // Generate 100 tokens and ensure they're all unique
        repeat(100) {
            val token = rateLimitService.generateSessionToken()
            assertNotNull(token)
            assertTrue(tokens.add(token), "Token $token was not unique")
        }
    }

    @Test
    fun `should return full attempts for unknown identifier`() {
        assertEquals(5, rateLimitService.getRemainingAttempts("unknown-identifier"))
    }
}
