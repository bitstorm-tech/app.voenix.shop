package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.order.api.OrderPdfService
import com.jotoai.voenix.shop.order.api.OrderService
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemForPdfDto
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.api.dto.ArticlePdfData
import com.jotoai.voenix.shop.pdf.api.dto.MugDetailsPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Implementation of OrderPdfService that handles order-specific PDF generation.
 * This service orchestrates PDF generation by:
 * 1. Retrieving order data for PDF
 * 2. Converting order DTOs to PDF DTOs (moved from pdf module)
 * 3. Delegating actual PDF generation to the pdf module
 */
@Service
class OrderPdfServiceImpl(
    private val orderService: OrderService,
    private val pdfGenerationService: PdfGenerationService,
    private val articleQueryService: ArticleQueryService,
) : OrderPdfService {
    override fun generateOrderPdf(
        userId: Long,
        orderId: UUID,
    ): ByteArray {
        val orderForPdf = orderService.getOrderForPdf(userId, orderId)
        val orderPdfData = convertToOrderPdfData(orderForPdf)
        return pdfGenerationService.generateOrderPdf(orderPdfData)
    }

    override fun getOrderPdfFilename(
        userId: Long,
        orderId: UUID,
    ): String {
        val orderForPdf = orderService.getOrderForPdf(userId, orderId)
        return pdfGenerationService.getOrderPdfFilename(orderForPdf.orderNumber ?: "unknown")
    }

    /**
     * Converts OrderForPdfDto to OrderPdfData.
     * This method was moved from the pdf module to maintain proper module boundaries.
     * The pdf module should not know about order-specific DTOs.
     */
    private fun convertToOrderPdfData(orderForPdf: OrderForPdfDto): OrderPdfData =
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
