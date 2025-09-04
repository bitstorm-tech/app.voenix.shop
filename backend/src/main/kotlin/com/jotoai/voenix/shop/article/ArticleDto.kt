package com.jotoai.voenix.shop.article

import com.jotoai.voenix.shop.article.internal.dto.ShirtArticleVariantDto
import java.time.OffsetDateTime

data class ArticleDto(
    val id: Long,
    val name: String,
    val descriptionShort: String,
    val descriptionLong: String,
    val active: Boolean,
    val articleType: ArticleType,
    val categoryId: Long,
    val categoryName: String,
    val subcategoryId: Long? = null,
    val subcategoryName: String? = null,
    val supplierId: Long? = null,
    val supplierName: String? = null,
    val supplierArticleName: String? = null,
    val supplierArticleNumber: String? = null,
    val mugVariants: List<MugArticleVariantDto>? = null,
    val shirtVariants: List<ShirtArticleVariantDto>? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
