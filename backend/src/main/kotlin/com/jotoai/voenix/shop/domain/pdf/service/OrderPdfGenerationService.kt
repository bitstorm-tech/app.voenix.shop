package com.jotoai.voenix.shop.domain.pdf.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
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
    private val imageAccessService: ImageAccessService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OrderPdfGenerationService::class.java)

        // PDF page dimensions (A4 in points)
        private const val PAGE_WIDTH = 595f
        private const val PAGE_HEIGHT = 842f

        // Margins and layout
        private const val MARGIN = 50f
        private const val HEADER_HEIGHT = 30f
        private const val QR_CODE_SIZE = 80f
        private const val QR_CODE_MARGIN = 20f

        // Image sizing
        private const val MAX_IMAGE_WIDTH = PAGE_WIDTH - (2 * MARGIN)
        private const val MAX_IMAGE_HEIGHT = PAGE_HEIGHT - (2 * MARGIN) - HEADER_HEIGHT - QR_CODE_SIZE - (2 * QR_CODE_MARGIN)

        // QR code settings
        private const val QR_SIZE_PIXELS = 200
    }

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
        val page = PDPage(PDRectangle(PAGE_WIDTH, PAGE_HEIGHT))
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
     * Adds header text with order number and page information
     */
    private fun addHeader(
        contentStream: PDPageContentStream,
        orderNumber: String,
        pageNumber: Int,
        totalPages: Int,
    ) {
        contentStream.beginText()
        val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        contentStream.setFont(boldFont, 16f)

        val headerText = "Order $orderNumber ($pageNumber/$totalPages)"
        val textWidth = boldFont.getStringWidth(headerText) / 1000 * 16f
        val xPosition = (PAGE_WIDTH - textWidth) / 2
        val yPosition = PAGE_HEIGHT - MARGIN - 20f

        contentStream.newLineAtOffset(xPosition, yPosition)
        contentStream.showText(headerText)
        contentStream.endText()
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

            // Create PDF image object
            val pdfImage = PDImageXObject.createFromByteArray(document, imageData, "ProductImage")

            // Calculate dimensions to fit within available space while maintaining aspect ratio
            val imageWidth = pdfImage.width.toFloat()
            val imageHeight = pdfImage.height.toFloat()
            val aspectRatio = imageWidth / imageHeight

            val (scaledWidth, scaledHeight) =
                when {
                    imageWidth > imageHeight -> {
                        // Landscape: fit to width
                        val width = minOf(MAX_IMAGE_WIDTH, imageWidth)
                        Pair(width, width / aspectRatio)
                    }
                    else -> {
                        // Portrait or square: fit to height
                        val height = minOf(MAX_IMAGE_HEIGHT, imageHeight)
                        Pair(height * aspectRatio, height)
                    }
                }

            // Center the image
            val xPosition = (PAGE_WIDTH - scaledWidth) / 2
            val yPosition = (PAGE_HEIGHT - scaledHeight) / 2

            contentStream.drawImage(pdfImage, xPosition, yPosition, scaledWidth, scaledHeight)

            logger.debug("Added product image with dimensions ${scaledWidth}x$scaledHeight at position ($xPosition, $yPosition)")
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
            val xPosition = MARGIN
            val yPosition = MARGIN

            contentStream.drawImage(qrImage, xPosition, yPosition, QR_CODE_SIZE, QR_CODE_SIZE)

            logger.debug("Added QR code for order ID $orderId at position ($xPosition, $yPosition)")
        } catch (e: Exception) {
            logger.error("Failed to generate QR code for order ID $orderId", e)
            // Add fallback text
            addPlaceholderText(contentStream, "Order ID: $orderId", MARGIN, MARGIN + 10f)
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
        x: Float = PAGE_WIDTH / 2,
        y: Float = PAGE_HEIGHT / 2,
    ) {
        try {
            contentStream.beginText()
            val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            contentStream.setFont(regularFont, 12f)

            if (x == PAGE_WIDTH / 2) {
                // Center the text
                val textWidth = regularFont.getStringWidth(text) / 1000 * 12f
                contentStream.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, y)
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
