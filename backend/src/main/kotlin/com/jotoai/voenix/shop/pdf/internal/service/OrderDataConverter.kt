package com.jotoai.voenix.shop.pdf.internal.service

import com.jotoai.voenix.shop.domain.orders.entity.Order
import com.jotoai.voenix.shop.domain.orders.entity.OrderItem
import com.jotoai.voenix.shop.pdf.api.dto.ArticlePdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import org.springframework.stereotype.Component

/**
 * Internal converter service for transforming Order entities to PDF DTOs.
 * This keeps the entity dependencies isolated within the PDF module.
 */
@Component
class OrderDataConverter {
    fun convertToOrderPdfData(order: Order): OrderPdfData =
        OrderPdfData(
            id = requireNotNull(order.id) { "Order ID cannot be null for order ${order.orderNumber}" },
            orderNumber = order.orderNumber,
            userId = requireNotNull(order.user.id) { "User ID cannot be null for order ${order.orderNumber}" },
            items = order.items.map { convertToOrderItemPdfData(it) },
        )

    private fun convertToOrderItemPdfData(orderItem: OrderItem): OrderItemPdfData =
        OrderItemPdfData(
            id = requireNotNull(orderItem.id) { "OrderItem ID cannot be null" },
            quantity = orderItem.quantity,
            generatedImageFilename = orderItem.generatedImageFilename,
            article = convertToArticlePdfData(orderItem.article),
        )

    private fun convertToArticlePdfData(article: com.jotoai.voenix.shop.domain.articles.entity.Article): ArticlePdfData =
        ArticlePdfData(
            id = requireNotNull(article.id) { "Article ID cannot be null" },
            mugDetails = null, // Will be populated by service layer as needed
        )
}
