package com.jotoai.voenix.shop.pdf.events

import java.time.Instant

/**
 * Event published when PDF generation is requested.
 */
data class PdfGenerationRequestedEvent(
    val articleId: Long?,
    val orderId: String?,
    val generationType: PdfGenerationType,
    val requestedBy: String?,
    val timestamp: Instant = Instant.now(),
)
