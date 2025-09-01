package com.jotoai.voenix.shop.article.internal.web.dto

import org.springframework.web.multipart.MultipartFile

data class MugVariantImageUploadRequest(
    val image: MultipartFile,
    val cropX: Double? = null,
    val cropY: Double? = null,
    val cropWidth: Double? = null,
    val cropHeight: Double? = null,
)
