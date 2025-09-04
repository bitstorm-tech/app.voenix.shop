package com.jotoai.voenix.shop.application

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

/**
 * Utility component for resolving client IP addresses from HTTP requests.
 * Handles common proxy headers like X-Forwarded-For and X-Real-IP.
 */
@Component
class ClientIpResolver {
    /**
     * Resolves the client IP address from the HTTP request.
     * Checks proxy headers first, then falls back to remote address.
     */
    fun resolve(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")

        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(',').first().trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr
        }
    }
}
