package com.jotoai.voenix.shop.cart

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import java.time.OffsetDateTime

data class CartItemDto(
    val id: Long,
    val article: ArticleDto,
    val variant: MugArticleVariantDto,
    val quantity: Int,
    val priceAtTime: Long, // Price in cents when added to cart
    val originalPrice: Long, // Current price for comparison
    val hasPriceChanged: Boolean,
    val totalPrice: Long, // priceAtTime * quantity
    val customData: Map<String, Any>, // Only for crop data and similar non-FK fields
    val generatedImageId: Long? = null,
    val generatedImageFilename: String? = null, // Filename for the generated image
    val promptId: Long? = null,
    val position: Int,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
