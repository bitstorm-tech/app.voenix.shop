/*
 * PDF generation functionality is temporarily disabled due to memory and performance issues.
 * This service is preserved for future reactivation when improved implementation is ready.
 * Controllers now return HTTP 503 Service Unavailable instead of calling these services.
 */

package com.jotoai.voenix.shop.domain.pdf.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import com.jotoai.voenix.shop.domain.pdf.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.domain.pdf.dto.PublicPdfGenerationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PublicPdfService(
    private val pdfService: PdfService,
    private val articleService: ArticleService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicPdfService::class.java)
    }

    fun generatePublicPdf(request: PublicPdfGenerationRequest): ByteArray {
        val article =
            try {
                articleService.findById(request.mugId)
            } catch (e: ResourceNotFoundException) {
                throw BadRequestException("Mug not found or unavailable")
            }

        if (article.mugDetails == null) {
            throw BadRequestException("The specified article is not a mug")
        }

        if (!article.active) {
            throw BadRequestException("This mug is currently unavailable")
        }

        val filename = request.imageUrl.substringAfterLast("/")

        logger.info("Processing public PDF generation for mug ID: ${request.mugId}")

        try {
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
}
