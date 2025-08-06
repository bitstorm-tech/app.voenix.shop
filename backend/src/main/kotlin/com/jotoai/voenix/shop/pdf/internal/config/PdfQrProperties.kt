package com.jotoai.voenix.shop.pdf.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * Configuration properties for PDF QR code generation.
 * Provides centralized configuration for QR code base URL and other QR-related settings.
 */
@ConfigurationProperties(prefix = "pdf.qr")
data class PdfQrProperties
    @ConstructorBinding
    constructor(
        /**
         * Base URL used for generating QR codes in PDFs.
         * Must be configured via properties, no default value to prevent misconfiguration.
         */
        val baseUrl: String,
    ) {
        /**
         * Generates a complete QR code URL by appending the provided path to the base URL.
         *
         * @param path The path to append to the base URL
         * @return The complete QR code URL
         */
        fun generateQrUrl(path: String): String {
            require(baseUrl.isNotBlank()) { "Base URL must be configured for PDF QR code generation" }
            val cleanBaseUrl = baseUrl.trimEnd('/')
            val cleanPath = if (path.startsWith("/")) path else "/$path"
            return "$cleanBaseUrl$cleanPath"
        }
    }
