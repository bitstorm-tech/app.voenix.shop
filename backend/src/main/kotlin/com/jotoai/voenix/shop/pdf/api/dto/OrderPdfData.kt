package com.jotoai.voenix.shop.pdf.api.dto

import java.util.UUID

/**
 * DTO containing order data needed for PDF generation.
 * This prevents direct dependency on Order entities from other modules.
 */
data class OrderPdfData(
    val id: UUID,
    val orderNumber: String?,
    val userId: Long,
    val items: List<OrderItemPdfData>,
) {
    fun getTotalItemCount(): Int = items.sumOf { it.quantity }
}

data class OrderItemPdfData(
    val id: UUID,
    val quantity: Int,
    val generatedImageFilename: String?,
    val article: ArticlePdfData,
)

data class ArticlePdfData(
    val id: Long,
    val mugDetails: MugDetailsPdfData?,
)

data class MugDetailsPdfData(
    val printTemplateWidthMm: Int,
    val printTemplateHeightMm: Int,
    val documentFormatWidthMm: Int?,
    val documentFormatHeightMm: Int?,
    val documentFormatMarginBottomMm: Int?,
)
