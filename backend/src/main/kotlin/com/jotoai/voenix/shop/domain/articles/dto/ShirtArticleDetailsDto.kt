package com.jotoai.voenix.shop.domain.articles.dto

import com.jotoai.voenix.shop.domain.articles.enums.FitType
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
