package com.jotoai.voenix.shop.domain.pdf.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.domain.articles.service.MugDetailsService
import com.jotoai.voenix.shop.domain.images.service.ImageAccessService
import com.jotoai.voenix.shop.domain.orders.entity.Order
import com.jotoai.voenix.shop.domain.orders.entity.OrderItem
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
class OrderPdfGenerationService(
    @param:Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String,
    @param:Value("\${pdf.size.width:239}") private val pdfWidthMm: Float,
    @param:Value("\${pdf.size.height:99}") private val pdfHeightMm: Float,
    @param:Value("\${pdf.margin:1}") private val pdfMarginMm: Float,
    private val imageAccessService: ImageAccessService,
    private val mugDetailsService: MugDetailsService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OrderPdfGenerationService::class.java)

        // Conversion factor from millimeters to PDF points
        private const val MM_TO_POINTS = 2.834645669f

        // Layout constants
        private const val HEADER_HEIGHT = 30f
        private const val QR_CODE_SIZE = 40f // Reduced by 50% from 80f
        private const val QR_CODE_MARGIN = 20f

        // QR code settings
        private const val QR_SIZE_PIXELS = 100 // Reduced by 50% from 200
    }

    // Calculate PDF dimensions in points
    private val pageWidth: Float get() = pdfWidthMm * MM_TO_POINTS
    private val pageHeight: Float get() = pdfHeightMm * MM_TO_POINTS
    private val margin: Float get() = pdfMarginMm * MM_TO_POINTS

    /**
     * Generates a PDF for the given order.
     * Creates one page per item quantity with the product image, header text, and QR code.
     *
     * @param order The order entity
     * @return PDF as byte array
     */
    fun generateOrderPdf(order: Order): ByteArray {
        logger.info("Generating PDF for order ${order.orderNumber} with ${order.getTotalItemCount()} total items")

        val document = PDDocument()
        return try {
            var pageNumber = 1
            val totalPages = order.getTotalItemCount()

            // Generate pages for each item quantity
            order.items.forEach { orderItem ->
                repeat(orderItem.quantity) {
                    createPage(document, order, orderItem, pageNumber, totalPages)
                    pageNumber++
                }
            }

            // Save document to byte array
            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("Failed to generate PDF for order ${order.orderNumber}", e)
            throw RuntimeException("Failed to generate PDF for order ${order.orderNumber}: ${e.message}", e)
        } finally {
            document.close()
        }
    }

    /**
     * Creates a single page with product image, header, and QR code
     */
    private fun createPage(
        document: PDDocument,
        order: Order,
        orderItem: OrderItem,
        pageNumber: Int,
        totalPages: Int,
    ) {
        val page = PDPage(PDRectangle(pageWidth, pageHeight))
        document.addPage(page)

        PDPageContentStream(document, page).use { contentStream ->
            // Add header with order number and page info
            addHeader(contentStream, order.orderNumber ?: "UNKNOWN", pageNumber, totalPages)

            // Add product image (centered)
            addProductImage(document, contentStream, order, orderItem)

            // Add QR code in bottom left
            addQrCode(document, contentStream, order.id.toString())
        }

        logger.debug("Created page $pageNumber/$totalPages for order ${order.orderNumber}")
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
        val fontSize = 14f

        // Create the combined text on one line
        val headerText = "$orderNumber ($pageNumber/$totalPages)"
        val textWidth = boldFont.getStringWidth(headerText) / 1000 * fontSize

        // Save the current graphics state
        contentStream.saveGraphicsState()

        // Position for the rotated text on the left side
        // The text will be rotated 90 degrees clockwise
        val xPosition = margin + 15f // Add more distance from left edge of page for better margin
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
        order: Order,
        orderItem: OrderItem,
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
                                    requireNotNull(order.user.id) { "User ID cannot be null for order ${order.orderNumber}" },
                                ).first
                        } catch (e: Exception) {
                            logger.warn(
                                "Could not load generated image ${orderItem.generatedImageFilename} for order ${order.orderNumber}, using placeholder",
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
            val mugDetails =
                mugDetailsService.findByArticleId(
                    requireNotNull(orderItem.article.id) { "Article ID cannot be null for order item ${orderItem.id}" },
                )

            // Create PDF image object
            val pdfImage = PDImageXObject.createFromByteArray(document, imageData, "ProductImage")

            // Use exact print template dimensions from MugArticleDetails
            // Never scale or correct the image size - use exact dimensions from database
            val imageWidthMm = mugDetails?.printTemplateWidthMm?.toFloat() ?: (pdfWidthMm - (2 * pdfMarginMm))
            val imageHeightMm = mugDetails?.printTemplateHeightMm?.toFloat() ?: (pdfHeightMm - (2 * pdfMarginMm) - 15f)

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
            // Add placeholder text instead
            addPlaceholderText(contentStream, "Image not available")
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
            ImageIO.write(bufferedImage, "PNG", qrByteArray)

            val qrImage = PDImageXObject.createFromByteArray(document, qrByteArray.toByteArray(), "QRCode")

            // Position QR code in bottom left corner
            val xPosition = margin
            val yPosition = margin

            contentStream.drawImage(qrImage, xPosition, yPosition, QR_CODE_SIZE, QR_CODE_SIZE)

            logger.debug("Added QR code for order ID $orderId at position ($xPosition, $yPosition)")
        } catch (e: Exception) {
            logger.error("Failed to generate QR code for order ID $orderId", e)
            // Add fallback text
            addPlaceholderText(contentStream, "Order ID: $orderId", margin, margin + 10f)
        }
    }

    /**
     * Creates a simple placeholder image when product image is not available
     */
    private fun createPlaceholderImage(): ByteArray {
        val width = 400
        val height = 300
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
        val font = java.awt.Font("Arial", java.awt.Font.BOLD, 24)
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
        ImageIO.write(image, "PNG", outputStream)
        return outputStream.toByteArray()
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
            contentStream.setFont(regularFont, 12f)

            if (x == pageWidth / 2) {
                // Center the text
                val textWidth = regularFont.getStringWidth(text) / 1000 * 12f
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
    fun getOrderPdfFilename(orderNumber: String): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "order_${orderNumber}_$timestamp.pdf"
    }
}
