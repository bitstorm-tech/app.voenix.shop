package com.jotoai.voenix.shop.domain.pdf.dto

data class PdfResponse(
    val filename: String,
    val size: Long,
    val contentType: String = "application/pdf",
)
