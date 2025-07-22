package com.jotoai.voenix.shop.domain.articles.dto

import com.jotoai.voenix.shop.domain.articles.enums.VariantType
import java.time.OffsetDateTime

data class ArticleVariantDto(
    val id: Long,
    val articleId: Long,
    val variantType: VariantType,
    val variantValue: String,
    val sku: String? = null,
    val exampleImageUrl: String? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
