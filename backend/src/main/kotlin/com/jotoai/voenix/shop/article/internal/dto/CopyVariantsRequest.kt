package com.jotoai.voenix.shop.article.internal.dto

import jakarta.validation.constraints.NotEmpty

data class CopyVariantsRequest(
    @field:NotEmpty(message = "At least one variant must be selected")
    val variantIds: List<Long>,
)
