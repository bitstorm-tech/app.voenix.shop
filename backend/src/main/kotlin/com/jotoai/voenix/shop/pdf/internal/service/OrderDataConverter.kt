package com.jotoai.voenix.shop.pdf.internal.service

import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemForPdfDto
import com.jotoai.voenix.shop.pdf.api.dto.ArticlePdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import org.springframework.stereotype.Component

/**
 * Internal converter service for transforming Order DTOs to PDF DTOs.
 * This eliminates dependencies on internal Order entities from the PDF module.
 */
@Component
class OrderDataConverter {
    fun convertToOrderPdfData(orderForPdf: OrderForPdfDto): OrderPdfData =
        OrderPdfData(
            id = orderForPdf.id,
            orderNumber = orderForPdf.orderNumber,
            userId = orderForPdf.userId,
            items = orderForPdf.items.map { convertToOrderItemPdfData(it) },
        )

    private fun convertToOrderItemPdfData(orderItem: OrderItemForPdfDto): OrderItemPdfData =
        OrderItemPdfData(
            id = orderItem.id,
            quantity = orderItem.quantity,
            generatedImageFilename = orderItem.generatedImageFilename,
            article =
                ArticlePdfData(
                    id = orderItem.articleId,
                    mugDetails = null, // Will be populated by service layer as needed
                ),
        )
}
