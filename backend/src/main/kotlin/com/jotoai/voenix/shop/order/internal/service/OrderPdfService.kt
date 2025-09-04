package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.article.api.ArticleService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.order.internal.dto.OrderForPdfDto
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.api.dto.ArticlePdfData
import com.jotoai.voenix.shop.pdf.api.dto.MugDetailsPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service that handles order-specific PDF generation.
 * This service orchestrates PDF generation by:
 * 1. Retrieving order data for PDF
 * 2. Converting order DTOs to PDF DTOs (moved from pdf module)
 * 3. Delegating actual PDF generation to the pdf module
 */
@Service
class OrderPdfService(
    private val orderService: OrderServiceImpl,
    private val pdfGenerationService: PdfGenerationService,
    private val articleService: ArticleService,
) {
    fun generateOrderPdf(
        userId: Long,
        orderId: UUID,
    ): ByteArray {
        val orderForPdf = orderService.getOrderForPdf(userId, orderId)
        val orderPdfData = convertToOrderPdfData(orderForPdf)
        return pdfGenerationService.generateOrderPdf(orderPdfData)
    }

    fun getOrderPdfFilename(
        userId: Long,
        orderId: UUID,
    ): String {
        val orderForPdf = orderService.getOrderForPdf(userId, orderId)
        return pdfGenerationService.getOrderPdfFilename(orderForPdf.orderNumber)
    }

    /**
     * Converts OrderForPdfDto to OrderPdfData.
     * This method was moved from the pdf module to maintain proper module boundaries.
     * The pdf module should not know about order-specific DTOs.
     */
    private fun convertToOrderPdfData(orderForPdf: OrderForPdfDto): OrderPdfData {
        if (orderForPdf.items.isEmpty()) {
            return OrderPdfData(
                id = orderForPdf.id,
                orderNumber = orderForPdf.orderNumber,
                userId = orderForPdf.userId,
                items = emptyList(),
            )
        }

        val pdfItemData = fetchAllPdfItemData(orderForPdf.items)
        val pdfItems = orderForPdf.items.map { it.toPdfData(pdfItemData) }

        return OrderPdfData(
            id = orderForPdf.id,
            orderNumber = orderForPdf.orderNumber,
            userId = orderForPdf.userId,
            items = pdfItems,
        )
    }

    private fun fetchAllPdfItemData(items: List<OrderForPdfDto.PdfItem>): PdfItemData {
        val articleIds = items.map { it.articleId }.distinct()
        val variantIds = items.map { it.variantId }.distinct()

        return PdfItemData(
            articles = articleService.getArticlesByIds(articleIds),
            variants = articleService.getMugVariantsByIds(variantIds),
            mugDetails = fetchMugDetailsBatch(articleIds),
        )
    }

    private fun fetchMugDetailsBatch(articleIds: List<Long>): Map<Long, MugArticleDetailsDto> {
        // Note: While this is still making individual calls, it only makes one call per unique articleId
        // and the distinct() call in fetchAllPdfItemData ensures we don't fetch the same data multiple times
        return articleIds
            .mapNotNull { articleId ->
                articleService.getMugDetailsByArticleId(articleId)?.let { articleId to it }
            }.toMap()
    }

    private fun OrderForPdfDto.PdfItem.toPdfData(itemData: PdfItemData): OrderItemPdfData {
        val article = itemData.articles[articleId]
        val variant = itemData.variants[variantId]
        val mugDetails = itemData.mugDetails[articleId]

        return OrderItemPdfData(
            id = UUID.randomUUID(),
            quantity = quantity,
            generatedImageFilename = imageFilename,
            article =
                ArticlePdfData(
                    id = articleId,
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
            variantId = variantId,
            variantName = variant?.name,
        )
    }

    private data class PdfItemData(
        val articles: Map<Long, ArticleDto>,
        val variants: Map<Long, MugArticleVariantDto>,
        val mugDetails: Map<Long, MugArticleDetailsDto>,
    )
}
