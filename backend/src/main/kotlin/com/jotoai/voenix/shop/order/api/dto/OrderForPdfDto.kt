package com.jotoai.voenix.shop.order.api.dto

import java.util.UUID

/**
 * DTO containing order data needed for external processing like PDF generation.
 * This prevents direct dependency on Order entities from other modules while
 * providing all necessary data for external integrations.
 */
data class OrderForPdfDto(
    val id: UUID,
    val orderNumber: String?,
    val userId: Long,
    val items: List<OrderItemForPdfDto>,
)

data class OrderItemForPdfDto(
    val id: UUID,
    val quantity: Int,
    val generatedImageId: Long?,
    val generatedImageFilename: String?, // Dynamically fetched from ImageQueryService
    val articleId: Long,
    val variantId: Long,
)
