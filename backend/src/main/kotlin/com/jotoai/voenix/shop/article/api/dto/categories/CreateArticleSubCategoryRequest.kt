package com.jotoai.voenix.shop.article.api.dto.categories

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateArticleSubCategoryRequest(
    @field:NotNull(message = "Article category ID is required")
    @field:Positive(message = "Article category ID must be positive")
    val articleCategoryId: Long,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    val description: String?,
)
