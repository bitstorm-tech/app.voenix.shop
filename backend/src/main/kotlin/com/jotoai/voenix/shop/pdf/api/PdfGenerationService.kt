package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import com.jotoai.voenix.shop.pdf.api.dto.PublicPdfGenerationRequest

/**
 * Consolidated interface for all PDF generation operations.
 * This interface combines functionality from PdfFacade, PdfQueryService, OrderPdfService, and PublicPdfService
 * into a single, cohesive service contract.
 *
 * This consolidation enables:
 * - Better code reuse through shared utility methods
 * - Simplified dependency injection
 * - Consistent error handling across all PDF operations
 * - Easier testing and maintenance
 */
interface PdfGenerationService {
    /**
     * Generates a PDF document for the given request.
     * @param request The PDF generation request
     * @return The generated PDF as byte array
     */
    fun generatePdf(request: GeneratePdfRequest): ByteArray

    /**
     * Generates a PDF for the given order.
     * Creates one page per item quantity with product image, header, and QR code.
     * @param orderData The order data
     * @return The generated PDF as byte array
     */
    fun generateOrderPdf(orderData: OrderPdfData): ByteArray

    /**
     * Generates the PDF filename for an order.
     * @param orderNumber The order number
     * @return The generated filename
     */
    fun getOrderPdfFilename(orderNumber: String): String

    /**
     * Generates a PDF for public users.
     * @param request The public PDF generation request
     * @return The generated PDF as byte array
     */
    fun generatePublicPdf(request: PublicPdfGenerationRequest): ByteArray
}
