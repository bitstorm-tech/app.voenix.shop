package com.jotoai.voenix.shop.pdf.api.dto

data class PdfResponse(
    val filename: String,
    val size: Long,
    val contentType: String = "application/pdf",
)
