package com.jotoai.voenix.shop.pdf.events

import java.time.Instant

/**
 * Event published when a PDF has been successfully generated.
 */
data class PdfGeneratedEvent(
    val filename: String,
    val size: Long,
    val articleId: Long?,
    val orderId: String?,
    val generationType: PdfGenerationType,
    val timestamp: Instant = Instant.now(),
)

enum class PdfGenerationType {
    ARTICLE_PDF,
    ORDER_PDF,
    PUBLIC_PDF,
}
