package com.jotoai.voenix.shop.order.internal.dto

import java.util.UUID

/**
 * DTO containing order data needed for external processing like PDF generation.
 * This prevents direct dependency on Order entities from other modules while
 * providing all necessary data for external integrations.
 */
data class OrderForPdfDto(
    val id: UUID,
    val orderNumber: String,
    val userId: Long,
    val items: List<PdfItem>,
) {
    data class PdfItem(
        val quantity: Int,
        val imageFilename: String?,
        val articleId: Long,
        val variantId: Long,
    )
}
