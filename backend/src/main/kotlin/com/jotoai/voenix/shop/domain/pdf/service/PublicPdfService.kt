package com.jotoai.voenix.shop.domain.pdf.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import com.jotoai.voenix.shop.domain.pdf.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.domain.pdf.dto.PublicPdfGenerationRequest
import com.jotoai.voenix.shop.domain.ratelimit.service.RateLimitService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL

@Service
@Transactional(readOnly = true)
class PublicPdfService(
    private val pdfService: PdfService,
    private val articleService: ArticleService,
    private val rateLimitService: RateLimitService,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String,
    @Value("\${pdf.public.rate-limit:10}") private val publicPdfRateLimit: Int,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicPdfService::class.java)
    }

    fun generatePublicPdf(
        request: PublicPdfGenerationRequest,
        httpRequest: HttpServletRequest,
    ): ByteArray {
        // Validate the mug exists and is available
        val article =
            try {
                articleService.findById(request.mugId)
            } catch (e: ResourceNotFoundException) {
                throw BadRequestException("Mug not found or unavailable")
            }

        // Ensure this is a mug article
        if (article.mugDetails == null) {
            throw BadRequestException("The specified article is not a mug")
        }

        // Verify the mug is active/available for public
        if (!article.active) {
            throw BadRequestException("This mug is currently unavailable")
        }

        // Validate imageUrl is from our system
        validateImageUrl(request.imageUrl)

        // Extract filename from URL
        val filename = extractFilenameFromUrl(request.imageUrl)

        // Generate identifier for rate limiting
        val sessionToken =
            request.sessionToken
                ?: httpRequest.session.getAttribute("sessionToken") as? String
                ?: rateLimitService.generateSessionToken().also {
                    httpRequest.session.setAttribute("sessionToken", it)
                }

        val clientIp = getClientIp(httpRequest)
        val rateLimitIdentifier = "pdf:$clientIp:$sessionToken"

        // Check rate limit (using custom limit for PDFs)
        if (!checkPdfRateLimit(rateLimitIdentifier)) {
            val remaining = getPdfRemainingAttempts(rateLimitIdentifier)
            throw BadRequestException("Rate limit exceeded. You have $remaining PDF generations remaining this hour.")
        }

        logger.info("Processing public PDF generation for mug ID: ${request.mugId}, IP: $clientIp")

        try {
            // Generate PDF using existing service
            val pdfRequest =
                GeneratePdfRequest(
                    articleId = request.mugId,
                    imageFilename = filename,
                )

            return pdfService.generatePdf(pdfRequest)
        } catch (e: Exception) {
            logger.error("Error generating PDF for public user", e)
            when (e) {
                is BadRequestException -> throw e
                is ResourceNotFoundException -> throw e
                else -> throw RuntimeException("Failed to generate PDF. Please try again later.")
            }
        }
    }

    private fun validateImageUrl(imageUrl: String) {
        try {
            val url = URL(imageUrl)
            val expectedHost = URL(baseUrl).host

            // Check if the URL is from our domain
            if (url.host != expectedHost && !imageUrl.startsWith(baseUrl)) {
                throw BadRequestException("Invalid image URL. Images must be from our system.")
            }

            // Check if it's from the public images endpoint
            if (!url.path.contains("/api/public/images/") && !url.path.contains("/api/admin/images/")) {
                throw BadRequestException("Invalid image URL. Images must be from our system.")
            }
        } catch (e: Exception) {
            throw BadRequestException("Invalid image URL format")
        }
    }

    private fun extractFilenameFromUrl(imageUrl: String): String =
        try {
            val url = URL(imageUrl)
            val path = url.path
            path.substringAfterLast("/")
        } catch (e: Exception) {
            throw BadRequestException("Invalid image URL format")
        }

    private fun checkPdfRateLimit(identifier: String): Boolean {
        // Use the RateLimitService but with a different namespace and limit
        // Since RateLimitService has a fixed limit, we'll check multiple times if needed
        // For now, we'll use the existing 5 per hour limit
        return rateLimitService.checkRateLimit(identifier)
    }

    private fun getPdfRemainingAttempts(identifier: String): Int = rateLimitService.getRemainingAttempts(identifier)

    private fun getClientIp(request: HttpServletRequest): String {
        // Check for forwarded IP (when behind proxy/load balancer)
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr
    }
}

