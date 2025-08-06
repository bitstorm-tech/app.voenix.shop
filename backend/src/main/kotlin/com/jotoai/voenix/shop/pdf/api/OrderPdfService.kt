package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData

/**
 * Service for order-specific PDF generation.
 * This interface defines operations for generating PDFs from order data.
 */
interface OrderPdfService {
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
}
