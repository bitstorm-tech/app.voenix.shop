package com.jotoai.voenix.shop.order.api

import java.util.UUID

/**
 * Service interface for order-specific PDF generation operations.
 * This service handles the conversion and orchestration of order PDF generation,
 * while delegating the actual PDF creation to the pdf module.
 */
interface OrderPdfService {
    /**
     * Generates a PDF for the given order.
     * @param userId The user ID (for validation)
     * @param orderId The order ID
     * @return The generated PDF as byte array
     */
    fun generateOrderPdf(
        userId: Long,
        orderId: UUID,
    ): ByteArray

    /**
     * Generates the PDF filename for an order.
     * @param userId The user ID (for validation)
     * @param orderId The order ID
     * @return The generated filename
     */
    fun getOrderPdfFilename(
        userId: Long,
        orderId: UUID,
    ): String
}
