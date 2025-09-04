package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.image.CountFilter
import com.jotoai.voenix.shop.image.ImageService
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
internal class RateLimitService(
    private val imageService: ImageService
) {
    companion object {
        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
    }

    fun checkPublicLimit(ipAddress: String) {
        val hourAgo = OffsetDateTime.now().minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(ipAddress = ipAddress, after = hourAgo))
        
        if (count >= PUBLIC_MAX_GENERATIONS_PER_HOUR) {
            throw BadRequestException("Rate limit exceeded. Max $PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour.")
        }
    }

    fun checkUserLimit(userId: Long) {
        val dayAgo = OffsetDateTime.now().minusHours(USER_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(userId = userId, after = dayAgo))
        
        if (count >= USER_MAX_GENERATIONS_PER_DAY) {
            throw BadRequestException("Rate limit exceeded. Max $USER_MAX_GENERATIONS_PER_DAY images per day.")
        }
    }
}