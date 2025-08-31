package com.jotoai.voenix.shop.pdf.internal.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import com.jotoai.voenix.shop.pdf.api.exceptions.PdfGenerationException
import com.jotoai.voenix.shop.pdf.internal.config.PdfConfig
import com.jotoai.voenix.shop.pdf.internal.util.PlaceholderImage
import com.lowagie.text.BadElementException
import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

/**
 * Simplified PDF generation service implementation.
 * This service focuses on core PDF generation functionality with minimal complexity.
 */
@Service
@Transactional(readOnly = true)
class PdfGenerationServiceImpl(
    private val pdfConfig: PdfConfig,
    private val imageService: ImageService,
) : PdfGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val IMAGE_FORMAT_PNG = "PNG"
        private const val DEFAULT_IMAGE_MARGIN_MM = 15f
        private const val FALLBACK_TEXT_OFFSET = 10f
        private const val LEFT_HEADER_X_OFFSET = 15f
        private const val RIGHT_INFO_X_OFFSET = 5f
        private const val TEXT_ROTATION_VERTICAL = 90f
    }

    override fun generateOrderPdf(orderData: OrderPdfData): ByteArray {
        logger.info {
            "Generating PDF for order ${orderData.orderNumber} with " +
                "${orderData.getTotalItemCount()} total items"
        }

        try {
            val outputStream = ByteArrayOutputStream()

            // Get dimensions from the first item to initialize document with correct size
            val firstItem = orderData.items.first()
            val firstPageWidth = getPageWidth(firstItem)
            val firstPageHeight = getPageHeight(firstItem)

            val document = Document(Rectangle(firstPageWidth, firstPageHeight))
            val writer = PdfWriter.getInstance(document, outputStream)
            document.open()

            var pageNumber = 1
            val totalPages = orderData.getTotalItemCount()

            // Generate pages for each item quantity
            orderData.items.forEach { orderItem ->
                repeat(orderItem.quantity) {
                    if (pageNumber > 1) {
                        // Set page size BEFORE creating new page
                        val itemPageWidth = getPageWidth(orderItem)
                        val itemPageHeight = getPageHeight(orderItem)
                        document.setPageSize(Rectangle(itemPageWidth, itemPageHeight))
                        document.newPage()
                    }
                    createOrderPage(writer, orderData, orderItem, pageNumber, totalPages)
                    pageNumber++
                }
            }

            document.close()
            return outputStream.toByteArray()
        } catch (e: IOException) {
            logger.error(e) { "I/O error while generating PDF for order ${orderData.orderNumber}" }
            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        } catch (e: WriterException) {
            logger.error(e) { "QR code generation error while generating PDF for order ${orderData.orderNumber}" }
            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid argument while generating PDF for order ${orderData.orderNumber}" }
            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        }
    }

    override fun getOrderPdfFilename(orderNumber: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "order_${orderNumber}_$timestamp.pdf"
    }

    @Suppress("ThrowsCount")
    private fun createOrderPage(
        writer: PdfWriter,
        orderData: OrderPdfData,
        orderItem: OrderItemPdfData,
        pageNumber: Int,
        totalPages: Int,
    ) {
        // Get document format dimensions for this specific item, fallback to configuration
        val itemPageWidth = getPageWidth(orderItem)
        val itemPageHeight = getPageHeight(orderItem)
        val itemMargin = getMargin(orderItem)

        try {
            val contentByte = writer.directContent

            // Add header with order number and page info
            addOrderHeader(
                contentByte,
                orderData.orderNumber ?: "UNKNOWN",
                pageNumber,
                totalPages,
                itemPageHeight,
            )

            // Add product information on the right side
            addProductInfo(
                contentByte,
                orderItem,
                itemPageWidth,
                itemPageHeight,
            )

            // Add product image (centered)
            addProductImage(
                contentByte,
                orderData,
                orderItem,
                PageLayout(
                    pageWidth = itemPageWidth,
                    pageHeight = itemPageHeight,
                    margin = itemMargin,
                ),
            )

            // Add QR code in bottom left
            addOrderQrCode(contentByte, orderData.id.toString(), itemMargin)
        } catch (e: IOException) {
            throw PdfGenerationException(
                "I/O error creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        } catch (e: WriterException) {
            throw PdfGenerationException(
                "QR code generation error creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        } catch (e: BadElementException) {
            throw PdfGenerationException(
                "PDF element error creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        } catch (e: DocumentException) {
            throw PdfGenerationException(
                "Document error creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        }

        logger.debug { "Created page $pageNumber/$totalPages for order ${orderData.orderNumber}" }
    }

    private fun getPageWidth(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatWidthMm
            ?.let { it * PdfConfig.MM_TO_POINTS }
            ?: pdfConfig.size.widthPt

    private fun getPageHeight(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatHeightMm
            ?.let { it * PdfConfig.MM_TO_POINTS }
            ?: pdfConfig.size.heightPt

    private fun getMargin(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatMarginBottomMm
            ?.let { it * PdfConfig.MM_TO_POINTS }
            ?: (pdfConfig.marginMm * PdfConfig.MM_TO_POINTS)

    private fun addOrderHeader(
        contentByte: PdfContentByte,
        orderNumber: String,
        pageNumber: Int,
        totalPages: Int,
        pageHeight: Float,
    ) {
        val baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        val fontSize = pdfConfig.fonts.headerSizePt

        // Create the combined text on one line
        val headerText = "$orderNumber ($pageNumber/$totalPages)"

        // Use showTextAligned for proper rotated text positioning
        contentByte.beginText()
        contentByte.setFontAndSize(baseFont, fontSize)
        contentByte.showTextAligned(
            PdfContentByte.ALIGN_CENTER, // Center alignment
            headerText, // The text
            LEFT_HEADER_X_OFFSET, // x position (half font size + small offset for better visual centering)
            pageHeight / 2, // y position (center of page)
            TEXT_ROTATION_VERTICAL, // 90-degree rotation
        )
        contentByte.endText()
    }

    private fun addProductInfo(
        contentByte: PdfContentByte,
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
    ) {
        val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        val fontSize = pdfConfig.fonts.headerSizePt

        // Build product info text lines
        val productInfoLines = mutableListOf<String>()

        orderItem.article.supplierArticleName?.let { name ->
            productInfoLines.add(name)
        }

        orderItem.article.supplierArticleNumber?.let { number ->
            productInfoLines.add(number)
        }

        orderItem.variantName?.let { variant ->
            productInfoLines.add(variant)
        }

        if (productInfoLines.isEmpty()) return

        // Combine all product info into one line
        val line = productInfoLines.joinToString(" | ")

        // Use showTextAligned for proper rotated text positioning at right edge
        contentByte.beginText()
        contentByte.setFontAndSize(baseFont, fontSize)
        contentByte.showTextAligned(
            PdfContentByte.ALIGN_CENTER, // Center alignment
            line, // The text
            pageWidth - RIGHT_INFO_X_OFFSET, // x position (distance from right edge)
            pageHeight / 2, // y position (center of page)
            TEXT_ROTATION_VERTICAL, // 90-degree rotation
        )
        contentByte.endText()
    }

    private data class PageLayout(
        val pageWidth: Float,
        val pageHeight: Float,
        val margin: Float,
    )

    private fun addProductImage(
        contentByte: PdfContentByte,
        orderData: OrderPdfData,
        orderItem: OrderItemPdfData,
        layout: PageLayout,
    ) {
        try {
            val imageData = loadProductImage(orderData, orderItem)
            val pdfImage = Image.getInstance(imageData)
            val dimensions = calculateImageDimensions(orderItem, layout.pageWidth, layout.pageHeight, layout.margin)

            positionAndAddImage(contentByte, pdfImage, dimensions, layout.pageWidth, layout.pageHeight)
            logImageAddition(dimensions)
        } catch (e: IOException) {
            handleImageAdditionError(e, contentByte, orderItem, layout.pageWidth, layout.pageHeight)
        } catch (e: BadElementException) {
            handleImageAdditionError(e, contentByte, orderItem, layout.pageWidth, layout.pageHeight)
        }
    }

    private fun loadProductImage(
        orderData: OrderPdfData,
        orderItem: OrderItemPdfData,
    ): ByteArray =
        orderItem.generatedImageFilename
            ?.let { filename ->
                runCatching { imageService.get(filename, orderData.userId).bytes }
                    .onFailure { e ->
                        logger.warn(e) {
                            "Could not load image $filename for order ${orderData.orderNumber}, using placeholder"
                        }
                    }.getOrNull()
            }
            ?: PlaceholderImage.DEFAULT_BYTES.also {
                logger.info { "No generated image for order item ${orderItem.id}, using placeholder" }
            }

    private fun calculateImageDimensions(
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
        margin: Float,
    ): ImageDimensions {
        val imageWidthMm =
            orderItem.article.mugDetails
                ?.printTemplateWidthMm
                ?.toFloat()
                ?: ((pageWidth / PdfConfig.MM_TO_POINTS) - (2 * (margin / PdfConfig.MM_TO_POINTS)))

        val imageHeightMm =
            orderItem.article.mugDetails
                ?.printTemplateHeightMm
                ?.toFloat()
                ?: (
                    (pageHeight / PdfConfig.MM_TO_POINTS) -
                        (2 * (margin / PdfConfig.MM_TO_POINTS)) -
                        DEFAULT_IMAGE_MARGIN_MM
                )

        val imageWidthPt = imageWidthMm * PdfConfig.MM_TO_POINTS
        val imageHeightPt = imageHeightMm * PdfConfig.MM_TO_POINTS

        return ImageDimensions(imageWidthPt, imageHeightPt)
    }

    private fun positionAndAddImage(
        contentByte: PdfContentByte,
        pdfImage: Image,
        dimensions: ImageDimensions,
        pageWidth: Float,
        pageHeight: Float,
    ) {
        pdfImage.scaleAbsolute(dimensions.width, dimensions.height)

        val xPosition = (pageWidth - dimensions.width) / 2
        val yPosition = (pageHeight - dimensions.height) / 2

        pdfImage.setAbsolutePosition(xPosition, yPosition)
        contentByte.addImage(pdfImage)
    }

    private fun logImageAddition(dimensions: ImageDimensions) {
        logger.debug {
            "Added product image with exact dimensions ${dimensions.width}x${dimensions.height} points"
        }
    }

    private fun handleImageAdditionError(
        e: Exception,
        contentByte: PdfContentByte,
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
    ) {
        when (e) {
            is IOException -> {
                logger.error(e) { "I/O error adding product image for order item ${orderItem.id}" }
                addPlaceholderTextSafely(contentByte, orderItem, pageWidth, pageHeight, e)
            }
            is IllegalArgumentException -> {
                logger.error(e) { "Invalid image data for order item ${orderItem.id}" }
                addPlaceholderTextSafely(contentByte, orderItem, pageWidth, pageHeight, e)
            }
            else -> throw e
        }
    }

    private fun addPlaceholderTextSafely(
        contentByte: PdfContentByte,
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
        originalException: Exception,
    ) {
        try {
            addPlaceholderText(contentByte, PlaceholderText("Image not available", pageWidth, pageHeight, 0f, 0f))
        } catch (placeholderException: IOException) {
            logger.error(placeholderException) { "Failed to add placeholder text for order item ${orderItem.id}" }
            throw PdfGenerationException(
                "Failed to add product image and placeholder for order item ${orderItem.id}",
                originalException,
            )
        }
    }

    private data class ImageDimensions(
        val width: Float,
        val height: Float,
    )

    private fun addOrderQrCode(
        contentByte: PdfContentByte,
        orderId: String,
        margin: Float,
    ) {
        try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix =
                qrCodeWriter.encode(
                    orderId,
                    BarcodeFormat.QR_CODE,
                    pdfConfig.qrCode.sizePixels,
                    pdfConfig.qrCode.sizePixels,
                )

            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            val qrByteArray = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, IMAGE_FORMAT_PNG, qrByteArray)

            val qrImage = Image.getInstance(qrByteArray.toByteArray())
            qrImage.scaleAbsolute(pdfConfig.qrCode.sizePt, pdfConfig.qrCode.sizePt)

            val xPosition = 0f
            val yPosition = 0f

            qrImage.setAbsolutePosition(xPosition, yPosition)
            contentByte.addImage(qrImage)

            logger.debug { "Added QR code for order ID $orderId at position ($xPosition, $yPosition)" }
        } catch (e: WriterException) {
            logger.error(e) { "QR code generation error for order ID $orderId" }
            try {
                addPlaceholderText(
                    contentByte,
                    PlaceholderText(
                        text = "Order ID: $orderId",
                        pageWidth = Float.MAX_VALUE,
                        pageHeight = Float.MAX_VALUE,
                        x = margin,
                        y = margin + FALLBACK_TEXT_OFFSET,
                    ),
                )
            } catch (placeholderException: IOException) {
                logger.error(placeholderException) { "Failed to add QR code fallback text for order ID $orderId" }
                throw PdfGenerationException("Failed to add QR code and fallback text for order ID $orderId", e)
            }
        } catch (e: IOException) {
            logger.error(e) { "I/O error generating QR code for order ID $orderId" }
            try {
                addPlaceholderText(
                    contentByte,
                    PlaceholderText(
                        text = "Order ID: $orderId",
                        pageWidth = Float.MAX_VALUE,
                        pageHeight = Float.MAX_VALUE,
                        x = margin,
                        y = margin + FALLBACK_TEXT_OFFSET,
                    ),
                )
            } catch (placeholderException: IOException) {
                logger.error(placeholderException) { "Failed to add QR code fallback text for order ID $orderId" }
                throw PdfGenerationException("Failed to add QR code and fallback text for order ID $orderId", e)
            }
        }
    }

    private data class PlaceholderText(
        val text: String,
        val pageWidth: Float,
        val pageHeight: Float,
        val x: Float? = null,
        val y: Float? = null,
    )

    private fun addPlaceholderText(
        contentByte: PdfContentByte,
        placement: PlaceholderText,
    ) {
        try {
            val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
            contentByte.beginText()
            contentByte.setFontAndSize(baseFont, pdfConfig.fonts.placeholderSizePt)

            val x = placement.x
            val y = placement.y ?: (placement.pageHeight / 2)
            if (x == null) {
                val textWidth = baseFont.getWidthPoint(placement.text, pdfConfig.fonts.placeholderSizePt)
                contentByte.moveText((placement.pageWidth - textWidth) / 2, y)
            } else {
                contentByte.moveText(x, y)
            }

            contentByte.showText(placement.text)
            contentByte.endText()
        } catch (e: IOException) {
            logger.error(e) { "Failed to add placeholder text: ${placement.text}" }
        }
    }
}
