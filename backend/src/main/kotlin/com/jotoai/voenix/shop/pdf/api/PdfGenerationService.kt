package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData

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
     * Converts OrderForPdfDto to OrderPdfData.
     * This method encapsulates the conversion logic and maintains module boundaries
     * by keeping the internal OrderDataConverter within the PDF module.
     * @param orderForPdf The order data for PDF generation
     * @return The converted OrderPdfData
     */
    fun convertToOrderPdfData(orderForPdf: com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto): OrderPdfData
}
