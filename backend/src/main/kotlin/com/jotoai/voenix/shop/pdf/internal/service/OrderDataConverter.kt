package com.jotoai.voenix.shop.pdf.internal.service

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemForPdfDto
import com.jotoai.voenix.shop.pdf.api.dto.ArticlePdfData
import com.jotoai.voenix.shop.pdf.api.dto.MugDetailsPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import org.springframework.stereotype.Component

/**
 * Internal converter service for transforming Order DTOs to PDF DTOs.
 * This eliminates dependencies on internal Order entities from the PDF module.
 */
@Component
class OrderDataConverter(
    private val articleQueryService: ArticleQueryService,
) {
    fun convertToOrderPdfData(orderForPdf: OrderForPdfDto): OrderPdfData =
        OrderPdfData(
            id = orderForPdf.id,
            orderNumber = orderForPdf.orderNumber,
            userId = orderForPdf.userId,
            items = orderForPdf.items.map { convertToOrderItemPdfData(it) },
        )

    private fun convertToOrderItemPdfData(orderItem: OrderItemForPdfDto): OrderItemPdfData {
        val mugDetails = articleQueryService.getMugDetailsByArticleId(orderItem.articleId)
        val article = articleQueryService.getArticlesByIds(listOf(orderItem.articleId))[orderItem.articleId]
        val variant = articleQueryService.getMugVariantsByIds(listOf(orderItem.variantId))[orderItem.variantId]

        return OrderItemPdfData(
            id = orderItem.id,
            quantity = orderItem.quantity,
            generatedImageFilename = orderItem.generatedImageFilename,
            article =
                ArticlePdfData(
                    id = orderItem.articleId,
                    mugDetails =
                        mugDetails?.let {
                            MugDetailsPdfData(
                                printTemplateWidthMm = it.printTemplateWidthMm,
                                printTemplateHeightMm = it.printTemplateHeightMm,
                                documentFormatWidthMm = it.documentFormatWidthMm,
                                documentFormatHeightMm = it.documentFormatHeightMm,
                                documentFormatMarginBottomMm = it.documentFormatMarginBottomMm,
                            )
                        },
                    supplierArticleName = article?.supplierArticleName,
                    supplierArticleNumber = article?.supplierArticleNumber,
                ),
            variantId = orderItem.variantId,
            variantName = variant?.name,
        )
    }
}
