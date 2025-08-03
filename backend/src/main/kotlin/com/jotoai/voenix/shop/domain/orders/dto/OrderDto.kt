package com.jotoai.voenix.shop.domain.orders.dto

import com.jotoai.voenix.shop.domain.orders.enums.OrderStatus
import java.time.OffsetDateTime
import java.util.UUID

data class OrderDto(
    val id: UUID,
    val orderNumber: String,
    val customerEmail: String,
    val customerFirstName: String,
    val customerLastName: String,
    val customerPhone: String? = null,
    val shippingAddress: AddressDto,
    val billingAddress: AddressDto? = null,
    val subtotal: Long, // In cents
    val taxAmount: Long, // In cents
    val shippingAmount: Long, // In cents
    val totalAmount: Long, // In cents
    val status: OrderStatus,
    val cartId: Long,
    val notes: String? = null,
    val items: List<OrderItemDto>,
    val pdfUrl: String, // URL to download the PDF
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
