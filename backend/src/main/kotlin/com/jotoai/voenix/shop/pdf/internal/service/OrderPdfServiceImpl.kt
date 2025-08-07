package com.jotoai.voenix.shop.pdf.internal.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.common.exception.PdfGenerationException
import com.jotoai.voenix.shop.domain.articles.service.MugDetailsService
import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.pdf.api.OrderPdfService
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import com.jotoai.voenix.shop.pdf.events.PdfGeneratedEvent
import com.jotoai.voenix.shop.pdf.events.PdfGenerationFailedEvent
import com.jotoai.voenix.shop.pdf.events.PdfGenerationType
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

/**
 * Service responsible for generating PDF documents for orders.
 * Creates one page per item quantity with product image, header, and QR code.
 */
@Service
class OrderPdfServiceImpl(
    @param:Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String,
    @param:Value("\${pdf.size.width:239}") private val pdfWidthMm: Float,
    @param:Value("\${pdf.size.height:99}") private val pdfHeightMm: Float,
    @param:Value("\${pdf.margin:1}") private val pdfMarginMm: Float,
    private val imageAccessService: ImageAccessService,
    private val mugDetailsService: MugDetailsService,
    private val eventPublisher: ApplicationEventPublisher,
) : OrderPdfService {
    companion object {
        private val logger = LoggerFactory.getLogger(OrderPdfServiceImpl::class.java)

        // Conversion factor from millimeters to PDF points
        private const val MM_TO_POINTS = 2.834645669f

        // Layout constants
        private const val HEADER_HEIGHT_POINTS = 30f
        private const val QR_CODE_SIZE_POINTS = 40f
        private const val QR_CODE_MARGIN_POINTS = 20f
        private const val HEADER_MARGIN_FROM_EDGE = 15f

        // QR code generation settings
        private const val QR_SIZE_PIXELS = 100

        // Font settings
        private const val HEADER_FONT_SIZE = 14f
        private const val PLACEHOLDER_FONT_SIZE = 12f

        // Image settings
        private const val PLACEHOLDER_IMAGE_WIDTH = 400
        private const val PLACEHOLDER_IMAGE_HEIGHT = 300
        private const val PLACEHOLDER_FONT_SIZE_PIXELS = 24

        // Image format constants
        private const val IMAGE_FORMAT_PNG = "PNG"
        private const val PDF_IMAGE_NAME_QR = "QRCode"
        private const val PDF_IMAGE_NAME_PRODUCT = "ProductImage"

        // Default image margins when mug details are not available
        private const val DEFAULT_IMAGE_MARGIN_MM = 15f
    }

    // Calculate PDF dimensions in points
    private val pageWidth: Float get() = pdfWidthMm * MM_TO_POINTS
    private val pageHeight: Float get() = pdfHeightMm * MM_TO_POINTS
    private val margin: Float get() = pdfMarginMm * MM_TO_POINTS

    /**
     * Generates a PDF for the given order data.
     * Creates one page per item quantity with the product image, header text, and QR code.
     *
     * @param orderData The order data
     * @return PDF as byte array
     */
    override fun generateOrderPdf(orderData: OrderPdfData): ByteArray {
        logger.info("Generating PDF for order ${orderData.orderNumber} with ${orderData.getTotalItemCount()} total items")

        try {
            eventPublisher.publishEvent(
                com.jotoai.voenix.shop.pdf.events.PdfGenerationRequestedEvent(
                    articleId = null,
                    orderId = orderData.id.toString(),
                    generationType = PdfGenerationType.ORDER_PDF,
                    requestedBy = "user_${orderData.userId}",
                ),
            )

            // Use try-with-resources for proper resource management
            PDDocument().use { document ->
                var pageNumber = 1
                val totalPages = orderData.getTotalItemCount()

                // Generate pages for each item quantity
                orderData.items.forEach { orderItem ->
                    repeat(orderItem.quantity) {
                        createPage(document, orderData, orderItem, pageNumber, totalPages)
                        pageNumber++
                    }
                }

                // Save document to byte array
                val outputStream = ByteArrayOutputStream()
                document.save(outputStream)
                val pdfBytes = outputStream.toByteArray()

                // Publish success event
                eventPublisher.publishEvent(
                    PdfGeneratedEvent(
                        filename = getOrderPdfFilename(orderData.orderNumber ?: "unknown"),
                        size = pdfBytes.size.toLong(),
                        articleId = null,
                        orderId = orderData.id.toString(),
                        generationType = PdfGenerationType.ORDER_PDF,
                    ),
                )

                return pdfBytes
            }
        } catch (e: Exception) {
            logger.error("Failed to generate PDF for order ${orderData.orderNumber}", e)

            // Publish failure event
            eventPublisher.publishEvent(
                PdfGenerationFailedEvent(
                    articleId = null,
                    orderId = orderData.id.toString(),
                    generationType = PdfGenerationType.ORDER_PDF,
                    errorMessage = e.message ?: "Unknown error",
                ),
            )

            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        }
    }

    /**
     * Creates a single page with product image, header, and QR code
     */
    private fun createPage(
        document: PDDocument,
        orderData: OrderPdfData,
        orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData,
        pageNumber: Int,
        totalPages: Int,
    ) {
        val page = PDPage(PDRectangle(pageWidth, pageHeight))
        document.addPage(page)

        try {
            PDPageContentStream(document, page).use { contentStream ->
                // Add header with order number and page info
                addHeader(contentStream, orderData.orderNumber ?: "UNKNOWN", pageNumber, totalPages)

                // Add product image (centered)
                addProductImage(document, contentStream, orderData, orderItem)

                // Add QR code in bottom left
                addQrCode(document, contentStream, orderData.id.toString())
            }
        } catch (e: Exception) {
            throw PdfGenerationException("Failed to create page $pageNumber for order ${orderData.orderNumber}", e)
        }

        logger.debug("Created page $pageNumber/$totalPages for order ${orderData.orderNumber}")
    }

    /**
     * Adds order number and page information
     * Text is rotated 90 degrees clockwise and positioned on the left side
     */
    private fun addHeader(
        contentStream: PDPageContentStream,
        orderNumber: String,
        pageNumber: Int,
        totalPages: Int,
    ) {
        val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val fontSize = HEADER_FONT_SIZE

        // Create the combined text on one line
        val headerText = "$orderNumber ($pageNumber/$totalPages)"
        val textWidth = boldFont.getStringWidth(headerText) / 1000 * fontSize

        // Save the current graphics state
        contentStream.saveGraphicsState()

        // Position for the rotated text on the left side
        // The text will be rotated 90 degrees clockwise
        val xPosition = margin + HEADER_MARGIN_FROM_EDGE
        val yPositionBase = pageHeight / 2 // Center vertically on the page

        // Move to the position where we want the text
        contentStream.transform(Matrix.getTranslateInstance(xPosition, yPositionBase))

        // Rotate 90 degrees clockwise around the current position
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(90.0), 0f, 0f))

        // Draw the combined text in one line
        contentStream.beginText()
        contentStream.setFont(boldFont, fontSize)
        // Center the text along its length (which becomes vertical after rotation)
        contentStream.newLineAtOffset(-textWidth / 2, 0f)
        contentStream.showText(headerText)
        contentStream.endText()

        // Restore the graphics state
        contentStream.restoreGraphicsState()
    }

    /**
     * Adds the product image centered on the page
     */
    private fun addProductImage(
        document: PDDocument,
        contentStream: PDPageContentStream,
        orderData: OrderPdfData,
        orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData,
    ) {
        try {
            // Get image data - try generated image first, then fallback to placeholder
            val imageData =
                when {
                    orderItem.generatedImageFilename != null -> {
                        try {
                            imageAccessService
                                .validateAccessAndGetImageData(
                                    orderItem.generatedImageFilename!!,
                                    orderData.userId,
                                ).first
                        } catch (e: Exception) {
                            logger.warn(
                                "Could not load generated image ${orderItem.generatedImageFilename} for order ${orderData.orderNumber}, using placeholder",
                                e,
                            )
                            createPlaceholderImage()
                        }
                    }
                    else -> {
                        logger.info("No generated image for order item ${orderItem.id}, using placeholder")
                        createPlaceholderImage()
                    }
                }

            // Get MugArticleDetails for the correct print template dimensions
            val mugDetails = mugDetailsService.findByArticleId(orderItem.article.id)

            // Create PDF image object
            val pdfImage = PDImageXObject.createFromByteArray(document, imageData, PDF_IMAGE_NAME_PRODUCT)

            // Use exact print template dimensions from MugArticleDetails
            // Never scale or correct the image size - use exact dimensions from database
            val imageWidthMm = mugDetails?.printTemplateWidthMm?.toFloat() ?: (pdfWidthMm - (2 * pdfMarginMm))
            val imageHeightMm = mugDetails?.printTemplateHeightMm?.toFloat() ?: (pdfHeightMm - (2 * pdfMarginMm) - DEFAULT_IMAGE_MARGIN_MM)

            // Convert exact dimensions to points (no scaling or aspect ratio correction)
            val imageWidthPt = imageWidthMm * MM_TO_POINTS
            val imageHeightPt = imageHeightMm * MM_TO_POINTS

            // Center the image on the page using exact dimensions
            val xPosition = (pageWidth - imageWidthPt) / 2
            val yPosition = (pageHeight - imageHeightPt) / 2

            // Draw image with exact dimensions from database - no scaling
            contentStream.drawImage(pdfImage, xPosition, yPosition, imageWidthPt, imageHeightPt)

            logger.debug(
                "Added product image with exact dimensions ${imageWidthPt}x$imageHeightPt points at position ($xPosition, $yPosition)",
            )
            logger.debug("Using exact print template dimensions: ${imageWidthMm}mm x ${imageHeightMm}mm from MugArticleDetails")
        } catch (e: Exception) {
            logger.error("Failed to add product image for order item ${orderItem.id}", e)
            try {
                // Add placeholder text instead
                addPlaceholderText(contentStream, "Image not available")
            } catch (placeholderException: Exception) {
                logger.error("Failed to add placeholder text for order item ${orderItem.id}", placeholderException)
                throw PdfGenerationException("Failed to add product image and placeholder for order item ${orderItem.id}", e)
            }
        }
    }

    /**
     * Adds QR code containing the order ID in the bottom left corner
     */
    private fun addQrCode(
        document: PDDocument,
        contentStream: PDPageContentStream,
        orderId: String,
    ) {
        try {
            // Generate QR code
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(orderId, BarcodeFormat.QR_CODE, QR_SIZE_PIXELS, QR_SIZE_PIXELS)

            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            val qrByteArray = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, IMAGE_FORMAT_PNG, qrByteArray)

            val qrImage = PDImageXObject.createFromByteArray(document, qrByteArray.toByteArray(), PDF_IMAGE_NAME_QR)

            // Position QR code in bottom left corner
            val xPosition = margin
            val yPosition = margin

            contentStream.drawImage(qrImage, xPosition, yPosition, QR_CODE_SIZE_POINTS, QR_CODE_SIZE_POINTS)

            logger.debug("Added QR code for order ID $orderId at position ($xPosition, $yPosition)")
        } catch (e: Exception) {
            logger.error("Failed to generate QR code for order ID $orderId", e)
            try {
                // Add fallback text
                addPlaceholderText(contentStream, "Order ID: $orderId", margin, margin + 10f)
            } catch (placeholderException: Exception) {
                logger.error("Failed to add QR code fallback text for order ID $orderId", placeholderException)
                throw PdfGenerationException("Failed to add QR code and fallback text for order ID $orderId", e)
            }
        }
    }

    /**
     * Creates a simple placeholder image when product image is not available
     */
    private fun createPlaceholderImage(): ByteArray {
        val width = PLACEHOLDER_IMAGE_WIDTH
        val height = PLACEHOLDER_IMAGE_HEIGHT
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        // Fill with light gray background
        graphics.color = java.awt.Color.LIGHT_GRAY
        graphics.fillRect(0, 0, width, height)

        // Add border
        graphics.color = java.awt.Color.DARK_GRAY
        graphics.drawRect(0, 0, width - 1, height - 1)

        // Add placeholder text
        graphics.color = java.awt.Color.BLACK
        val font = java.awt.Font("Arial", java.awt.Font.BOLD, PLACEHOLDER_FONT_SIZE_PIXELS)
        graphics.font = font
        val fontMetrics = graphics.getFontMetrics(font)
        val text = "No Image Available"
        val textWidth = fontMetrics.stringWidth(text)
        val textHeight = fontMetrics.ascent
        val x = (width - textWidth) / 2
        val y = (height + textHeight) / 2
        graphics.drawString(text, x, y)

        graphics.dispose()

        // Convert to byte array
        val outputStream = ByteArrayOutputStream()
        try {
            ImageIO.write(image, IMAGE_FORMAT_PNG, outputStream)
            return outputStream.toByteArray()
        } catch (e: Exception) {
            throw PdfGenerationException("Failed to create placeholder image", e)
        }
    }

    /**
     * Adds placeholder text when image cannot be rendered
     */
    private fun addPlaceholderText(
        contentStream: PDPageContentStream,
        text: String,
        x: Float = pageWidth / 2,
        y: Float = pageHeight / 2,
    ) {
        try {
            contentStream.beginText()
            val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            contentStream.setFont(regularFont, PLACEHOLDER_FONT_SIZE)

            if (x == pageWidth / 2) {
                // Center the text
                val textWidth = regularFont.getStringWidth(text) / 1000 * PLACEHOLDER_FONT_SIZE
                contentStream.newLineAtOffset((pageWidth - textWidth) / 2, y)
            } else {
                contentStream.newLineAtOffset(x, y)
            }

            contentStream.showText(text)
            contentStream.endText()
        } catch (e: Exception) {
            logger.error("Failed to add placeholder text: $text", e)
        }
    }

    /**
     * Generates the PDF filename for an order
     */
    override fun getOrderPdfFilename(orderNumber: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "order_${orderNumber}_$timestamp.pdf"
    }
}
