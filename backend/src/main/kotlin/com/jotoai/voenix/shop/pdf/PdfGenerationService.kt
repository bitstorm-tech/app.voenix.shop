package com.jotoai.voenix.shop.pdf

/**
 * PDF generation service interface.
 * This service handles low-level PDF creation operations and is used by other modules
 * as an infrastructure service.
 *
 * The service is designed to be module-agnostic, accepting only its own DTOs
 * to maintain proper module boundaries in the Spring Modulith architecture.
 */
interface PdfGenerationService {
    /**
     * Generates a PDF for the given order data.
     * Creates one page per item quantity with product image, header, and QR code.
     * @param orderData The order data (already converted to PDF DTOs)
     * @return The generated PDF as byte array
     */
    fun generateOrderPdf(orderData: OrderPdfData): ByteArray

    /**
     * Generates the PDF filename for an order.
     * @param orderNumber The order number
     * @return The generated filename
     */
    fun getOrderPdfFilename(orderNumber: String): String
}
