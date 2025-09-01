package com.jotoai.voenix.shop.openai.internal.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ImageGenerationResponse(
    @JsonProperty("imageUrls")
    val imageUrls: List<String>,
    @JsonProperty("generatedImageIds")
    val generatedImageIds: List<Long>,
)
