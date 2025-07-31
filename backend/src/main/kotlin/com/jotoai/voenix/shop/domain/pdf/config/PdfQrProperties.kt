package com.jotoai.voenix.shop.domain.pdf.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Configuration properties for PDF QR code generation.
 * Provides centralized configuration for QR code base URL and other QR-related settings.
 */
@Component
@ConfigurationProperties(prefix = "pdf.qr")
data class PdfQrProperties(
    /**
     * Base URL used for generating QR codes in PDFs.
     * Defaults to the application base URL if not specified.
     */
    var baseUrl: String = "",
) {
    /**
     * Generates a complete QR code URL by appending the provided path to the base URL.
     *
     * @param path The path to append to the base URL
     * @return The complete QR code URL
     */
    fun generateQrUrl(path: String): String {
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBaseUrl$cleanPath"
    }
}
