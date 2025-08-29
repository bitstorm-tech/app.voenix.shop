package com.jotoai.voenix.shop.pdf.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for PDF generation.
 * This consolidates all PDF-related configuration into a single, manageable class.
 */
@Component
@ConfigurationProperties("pdf")
data class PdfConfig(
    var size: PageSize = PageSize(),
    var marginMm: Float = 1f,
    var fonts: FontConfig = FontConfig(),
    var qrCode: QrCodeConfig = QrCodeConfig(),
) {
    data class PageSize(
        var widthMm: Float = 239f,
        var heightMm: Float = 99f,
    ) {
        val widthPt: Float get() = widthMm * MM_TO_POINTS
        val heightPt: Float get() = heightMm * MM_TO_POINTS
    }

    data class FontConfig(
        var headerSizePt: Float = 14f,
        var placeholderSizePt: Float = 12f,
    )

    data class QrCodeConfig(
        var sizePixels: Int = 100,
        var sizePt: Float = 40f,
    )

    companion object {
        const val MM_TO_POINTS = 2.8346457f
    }
}
