package com.jotoai.voenix.shop.article.api.dto

import com.jotoai.voenix.shop.article.api.enums.FitType
import java.time.OffsetDateTime

data class ShirtArticleDetailsDto(
    val articleId: Long,
    val material: String,
    val careInstructions: String? = null,
    val fitType: FitType,
    val availableSizes: List<String>,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
