package com.jotoai.voenix.shop.pdf.events

import java.time.Instant

/**
 * Event published when PDF generation fails.
 */
data class PdfGenerationFailedEvent(
    val articleId: Long?,
    val orderId: String?,
    val generationType: PdfGenerationType,
    val errorMessage: String,
    val timestamp: Instant = Instant.now(),
)
