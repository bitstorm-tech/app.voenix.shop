package com.jotoai.voenix.shop.order.api.dto

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import java.time.OffsetDateTime
import java.util.UUID

data class OrderItemDto(
    val id: UUID,
    val article: ArticleDto,
    val variant: MugArticleVariantDto,
    val quantity: Int,
    val pricePerItem: Long, // In cents
    val totalPrice: Long, // In cents
    val generatedImageId: Long? = null,
    val generatedImageFilename: String? = null,
    val promptId: Long? = null,
    val customData: Map<String, Any> = emptyMap(),
    val createdAt: OffsetDateTime,
)
