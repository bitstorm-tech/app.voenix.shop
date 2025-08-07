package com.jotoai.voenix.shop.image.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PublicImageGenerationResponse(
    @JsonProperty("imageUrls")
    val imageUrls: List<String>,
    @JsonProperty("generatedImageIds")
    val generatedImageIds: List<Long>,
)
