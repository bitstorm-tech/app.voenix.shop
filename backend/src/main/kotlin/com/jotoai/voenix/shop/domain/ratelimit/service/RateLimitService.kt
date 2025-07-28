package com.jotoai.voenix.shop.domain.ratelimit.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService {
    companion object {
        private val logger = LoggerFactory.getLogger(RateLimitService::class.java)
        private const val DEFAULT_RATE_LIMIT_PER_HOUR = 5
        private const val PDF_RATE_LIMIT_PER_HOUR = 10
        private const val CLEANUP_INTERVAL_MINUTES = 60L
    }

    data class RateLimitEntry(
        val attempts: MutableList<LocalDateTime> = mutableListOf(),
        var lastCleanup: LocalDateTime = LocalDateTime.now(),
    )

    private val rateLimitMap = ConcurrentHashMap<String, RateLimitEntry>()
    private var lastGlobalCleanup = LocalDateTime.now()

    fun checkRateLimit(identifier: String): Boolean {
        cleanupIfNeeded()

        val now = LocalDateTime.now()
        val entry = rateLimitMap.computeIfAbsent(identifier) { RateLimitEntry() }

        // Determine rate limit based on identifier prefix
        val rateLimit =
            when {
                identifier.startsWith("pdf:") -> PDF_RATE_LIMIT_PER_HOUR
                else -> DEFAULT_RATE_LIMIT_PER_HOUR
            }

        synchronized(entry) {
            // Remove attempts older than 1 hour
            entry.attempts.removeIf { attempt ->
                ChronoUnit.HOURS.between(attempt, now) >= 1
            }

            // Check if rate limit exceeded
            if (entry.attempts.size >= rateLimit) {
                logger.warn("Rate limit exceeded for identifier: $identifier")
                return false
            }

            // Add current attempt
            entry.attempts.add(now)
            return true
        }
    }

    fun getRemainingAttempts(identifier: String): Int {
        val now = LocalDateTime.now()

        // Determine rate limit based on identifier prefix
        val rateLimit =
            when {
                identifier.startsWith("pdf:") -> PDF_RATE_LIMIT_PER_HOUR
                else -> DEFAULT_RATE_LIMIT_PER_HOUR
            }

        val entry = rateLimitMap[identifier] ?: return rateLimit

        synchronized(entry) {
            entry.attempts.removeIf { attempt ->
                ChronoUnit.HOURS.between(attempt, now) >= 1
            }
            return rateLimit - entry.attempts.size
        }
    }

    fun generateSessionToken(): String = UUID.randomUUID().toString()

    private fun cleanupIfNeeded() {
        val now = LocalDateTime.now()
        if (ChronoUnit.MINUTES.between(lastGlobalCleanup, now) >= CLEANUP_INTERVAL_MINUTES) {
            synchronized(this) {
                if (ChronoUnit.MINUTES.between(lastGlobalCleanup, now) >= CLEANUP_INTERVAL_MINUTES) {
                    cleanup()
                    lastGlobalCleanup = now
                }
            }
        }
    }

    private fun cleanup() {
        logger.info("Running rate limit cleanup")
        val now = LocalDateTime.now()
        val keysToRemove = mutableListOf<String>()

        rateLimitMap.forEach { (key, entry) ->
            synchronized(entry) {
                entry.attempts.removeIf { attempt ->
                    ChronoUnit.HOURS.between(attempt, now) >= 1
                }
                if (entry.attempts.isEmpty()) {
                    keysToRemove.add(key)
                }
            }
        }

        keysToRemove.forEach { rateLimitMap.remove(it) }
        logger.info("Cleaned up ${keysToRemove.size} expired rate limit entries")
    }
}
