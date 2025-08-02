package com.jotoai.voenix.shop.domain.cart.dto

import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
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
    val customData: Map<String, Any>,
    val position: Int,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
