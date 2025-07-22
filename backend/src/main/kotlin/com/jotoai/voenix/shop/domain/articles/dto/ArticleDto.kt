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
    val variants: List<ArticleVariantDto> = emptyList(),
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
