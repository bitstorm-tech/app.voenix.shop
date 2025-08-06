package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.PublicPdfGenerationRequest

/**
 * Service for public PDF generation.
 * This interface defines operations for generating PDFs for public users.
 */
interface PublicPdfService {
    /**
     * Generates a PDF for public users.
     * @param request The public PDF generation request
     * @return The generated PDF as byte array
     */
    fun generatePublicPdf(request: PublicPdfGenerationRequest): ByteArray
}
