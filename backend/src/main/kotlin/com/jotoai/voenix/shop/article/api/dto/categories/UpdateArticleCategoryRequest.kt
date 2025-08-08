package com.jotoai.voenix.shop.article.api.dto.categories

import jakarta.validation.constraints.Size

data class UpdateArticleCategoryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    val description: String?,
)
