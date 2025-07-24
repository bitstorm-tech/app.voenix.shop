package com.jotoai.voenix.shop.domain.articles.dto

import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import java.time.OffsetDateTime

data class ArticleDto(
    val id: Long,
    val name: String,
    val descriptionShort: String,
    val descriptionLong: String,
    val exampleImageFilename: String,
    val price: Int,
    val active: Boolean,
    val articleType: ArticleType,
    val categoryId: Long,
    val categoryName: String,
    val subcategoryId: Long? = null,
    val subcategoryName: String? = null,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val mugVariants: List<ArticleMugVariantDto>? = null,
    val shirtVariants: List<ArticleShirtVariantDto>? = null,
    val pillowVariants: List<ArticlePillowVariantDto>? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
