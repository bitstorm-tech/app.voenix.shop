package com.jotoai.voenix.shop.pdf.internal.service

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.pdf.api.PdfFacade
import com.jotoai.voenix.shop.pdf.api.PublicPdfService
import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.pdf.api.dto.PublicPdfGenerationRequest
import com.jotoai.voenix.shop.pdf.api.exceptions.PdfGenerationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/*
 * PDF generation functionality is temporarily disabled due to memory and performance issues.
 * This service is preserved for future reactivation when improved implementation is ready.
 * Controllers now return HTTP 503 Service Unavailable instead of calling these services.
 */

@Service
@Transactional(readOnly = true)
class PublicPdfServiceImpl(
    private val pdfFacade: PdfFacade,
    private val articleQueryService: ArticleQueryService,
) : PublicPdfService {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicPdfServiceImpl::class.java)
    }

    override fun generatePublicPdf(request: PublicPdfGenerationRequest): ByteArray {
        try {
            val article =
                try {
                    articleQueryService.findById(request.mugId)
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

            val pdfRequest =
                GeneratePdfRequest(
                    articleId = request.mugId,
                    imageFilename = filename,
                )

            return pdfFacade.generatePdf(pdfRequest)
        } catch (e: BadRequestException) {
            logger.error("Bad request error generating PDF for public user", e)
            throw e
        } catch (e: ResourceNotFoundException) {
            logger.error("Resource not found error generating PDF for public user", e)
            throw e
        } catch (e: PdfGenerationException) {
            logger.error("PDF generation error for public user", e)
            throw RuntimeException("Failed to generate PDF. Please try again later.", e)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid argument error generating PDF for public user", e)
            throw BadRequestException("Invalid request parameters")
        } catch (e: IllegalStateException) {
            logger.error("Invalid state error generating PDF for public user", e)
            throw RuntimeException("Service temporarily unavailable. Please try again later.", e)
        }
    }
}
