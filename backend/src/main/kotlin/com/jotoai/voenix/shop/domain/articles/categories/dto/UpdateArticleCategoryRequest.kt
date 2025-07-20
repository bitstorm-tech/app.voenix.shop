package com.jotoai.voenix.shop.domain.articles.categories.dto

import jakarta.validation.constraints.Size

data class UpdateArticleCategoryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    val description: String?,
)
