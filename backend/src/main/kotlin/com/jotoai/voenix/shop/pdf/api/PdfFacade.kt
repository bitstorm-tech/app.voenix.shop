package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest

/**
 * Main facade for PDF module operations.
 * This interface defines general PDF generation operations.
 */
interface PdfFacade {
    /**
     * Generates a PDF document for the given request.
     * @param request The PDF generation request
     * @return The generated PDF as byte array
     */
    fun generatePdf(request: GeneratePdfRequest): ByteArray
}
