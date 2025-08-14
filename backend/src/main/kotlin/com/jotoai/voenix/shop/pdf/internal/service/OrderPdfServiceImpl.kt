package com.jotoai.voenix.shop.pdf.internal.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.pdf.api.OrderPdfService
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import com.jotoai.voenix.shop.pdf.api.exceptions.PdfGenerationException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
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

/**
 * Service responsible for generating PDF documents for orders.
 * Creates one page per item quantity with product image, header, and QR code.
 */
@Service
class OrderPdfServiceImpl(
    @param:Value("\${pdf.size.width:239}") private val pdfWidthMm: Float,
    @param:Value("\${pdf.size.height:99}") private val pdfHeightMm: Float,
    @param:Value("\${pdf.margin:1}") private val pdfMarginMm: Float,
    private val imageAccessService: ImageAccessService,
) : OrderPdfService {
    companion object {
        private val logger = LoggerFactory.getLogger(OrderPdfServiceImpl::class.java)

        // Conversion factor from millimeters to PDF points
        private const val MM_TO_POINTS = 2.834645669f

        // Layout constants
        private const val QR_CODE_SIZE_POINTS = 40f
        private const val HEADER_MARGIN_FROM_EDGE = 15f

        // QR code generation settings
        private const val QR_SIZE_PIXELS = 100

        // Font settings
        private const val HEADER_FONT_SIZE = 14f
        private const val PLACEHOLDER_FONT_SIZE = 12f
        private const val FONT_WIDTH_DIVISOR = 1000f

        // Image settings
        private const val PLACEHOLDER_IMAGE_WIDTH = 400
        private const val PLACEHOLDER_IMAGE_HEIGHT = 300
        private const val PLACEHOLDER_FONT_SIZE_PIXELS = 24

        // Math constants
        private const val DEGREES_90 = 90.0

        // Text positioning
        private const val FALLBACK_TEXT_OFFSET = 10f

        // Image format constants
        private const val IMAGE_FORMAT_PNG = "PNG"
        private const val PDF_IMAGE_NAME_QR = "QRCode"
        private const val PDF_IMAGE_NAME_PRODUCT = "ProductImage"

        // Default image margins when mug details are not available
        private const val DEFAULT_IMAGE_MARGIN_MM = 15f

        // Product info text layout
        private const val PRODUCT_INFO_LINE_HEIGHT_FACTOR = 1.2f
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
        logger.info(
            "Generating PDF for order ${orderData.orderNumber} with " +
                    "${orderData.getTotalItemCount()} total items",
        )

        try {
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

                return pdfBytes
            }
        } catch (e: IOException) {
            logger.error("I/O error while generating PDF for order ${orderData.orderNumber}", e)
            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        } catch (e: WriterException) {
            logger.error("QR code generation error while generating PDF for order ${orderData.orderNumber}", e)
            throw PdfGenerationException("Failed to generate PDF for order ${orderData.orderNumber}: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid argument while generating PDF for order ${orderData.orderNumber}", e)
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
        // Get document format dimensions for this specific item, fallback to configuration
        val itemPageWidth = getPageWidth(orderItem)
        val itemPageHeight = getPageHeight(orderItem)
        val itemMargin = getMargin(orderItem)

        val page = PDPage(PDRectangle(itemPageWidth, itemPageHeight))
        document.addPage(page)

        try {
            PDPageContentStream(document, page).use { contentStream ->
                // Add header with order number and page info
                addHeader(
                    contentStream,
                    orderData.orderNumber ?: "UNKNOWN",
                    pageNumber,
                    totalPages,
                    itemPageWidth,
                    itemPageHeight,
                    itemMargin,
                )

                // Add product information on the right side
                addProductInfo(
                    contentStream,
                    orderItem,
                    itemPageWidth,
                    itemPageHeight,
                    itemMargin,
                )

                // Add product image (centered)
                addProductImage(
                    document,
                    contentStream,
                    orderData,
                    orderItem,
                    itemPageWidth,
                    itemPageHeight,
                    itemMargin,
                )

                // Add QR code in bottom left
                addQrCode(document, contentStream, orderData.id.toString(), itemMargin)
            }
        } catch (e: IOException) {
            throw PdfGenerationException("I/O error creating page $pageNumber for order ${orderData.orderNumber}", e)
        } catch (e: WriterException) {
            throw PdfGenerationException(
                "QR code generation error creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        } catch (e: IllegalArgumentException) {
            throw PdfGenerationException(
                "Invalid argument creating page $pageNumber for order ${orderData.orderNumber}",
                e,
            )
        }

        logger.debug("Created page $pageNumber/$totalPages for order ${orderData.orderNumber}")
    }

    /**
     * Gets the page width for a specific order item, using document format or falling back to configuration
     */
    private fun getPageWidth(orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatWidthMm
            ?.let { it * MM_TO_POINTS }
            ?: (pdfWidthMm * MM_TO_POINTS)

    /**
     * Gets the page height for a specific order item, using document format or falling back to configuration
     */
    private fun getPageHeight(orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatHeightMm
            ?.let { it * MM_TO_POINTS }
            ?: (pdfHeightMm * MM_TO_POINTS)

    /**
     * Gets the margin for a specific order item, using document format or falling back to configuration
     */
    private fun getMargin(orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatMarginBottomMm
            ?.let { it * MM_TO_POINTS }
            ?: (pdfMarginMm * MM_TO_POINTS)

    /**
     * Adds order number and page information
     * Text is rotated 90 degrees clockwise and positioned on the left side
     */
    private fun addHeader(
        contentStream: PDPageContentStream,
        orderNumber: String,
        pageNumber: Int,
        totalPages: Int,
        pageWidth: Float,
        pageHeight: Float,
        margin: Float,
    ) {
        val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val fontSize = HEADER_FONT_SIZE

        // Create the combined text on one line
        val headerText = "$orderNumber ($pageNumber/$totalPages)"
        val textWidth = boldFont.getStringWidth(headerText) / FONT_WIDTH_DIVISOR * fontSize

        // Save the current graphics state
        contentStream.saveGraphicsState()

        // Position for the rotated text on the left side
        // The text will be rotated 90 degrees clockwise
        val xPosition = margin + HEADER_MARGIN_FROM_EDGE
        val yPositionBase = pageHeight / 2 // Center vertically on the page

        // Move to the position where we want the text
        contentStream.transform(Matrix.getTranslateInstance(xPosition, yPositionBase))

        // Rotate 90 degrees clockwise around the current position
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(DEGREES_90), 0f, 0f))

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
     * Adds product information (supplier mug name, supplier article number, variant name)
     * Text is rotated 90 degrees clockwise and positioned on the right side
     */
    private fun addProductInfo(
        contentStream: PDPageContentStream,
        orderItem: com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
        margin: Float,
    ) {
        val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val fontSize = HEADER_FONT_SIZE

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

        // Save the current graphics state
        contentStream.saveGraphicsState()

        // Position for the rotated text on the right side
        val xPosition = pageWidth - margin - HEADER_MARGIN_FROM_EDGE
        val yPositionBase = pageHeight / 2 // Center vertically on the page

        // Calculate total text height to center all lines
        val lineHeight = fontSize * PRODUCT_INFO_LINE_HEIGHT_FACTOR
        val totalTextHeight = productInfoLines.size * lineHeight
        var currentYOffset = totalTextHeight / 2

        val line = productInfoLines.joinToString(" | ")
        val textWidth = regularFont.getStringWidth(line) / FONT_WIDTH_DIVISOR * fontSize

        // Move to the position where we want the text
        contentStream.transform(Matrix.getTranslateInstance(xPosition, yPositionBase + currentYOffset))

        // Rotate 90 degrees clockwise around the current position
        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(DEGREES_90), 0f, 0f))

        // Draw the text
        contentStream.beginText()
        contentStream.setFont(regularFont, fontSize)
        // Center the text along its length (which becomes vertical after rotation)
        contentStream.newLineAtOffset(-textWidth / 2, 0f)
        contentStream.showText(line)
        contentStream.endText()

        // Reset transformation for next line
        contentStream.restoreGraphicsState()
        contentStream.saveGraphicsState()

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
        pageWidth: Float,
        pageHeight: Float,
        margin: Float,
    ) {
        try {
            // Get image data - try generated image first, then fallback to placeholder
            val imageData =
                when {
                    orderItem.generatedImageFilename != null -> {
                        try {
                            imageAccessService.getImageData(orderItem.generatedImageFilename!!, orderData.userId).first
                        } catch (e: IOException) {
                            logger.warn(
                                "Could not load generated image ${orderItem.generatedImageFilename} " +
                                        "for order ${orderData.orderNumber}, using placeholder",
                                e,
                            )
                            createPlaceholderImage()
                        } catch (e: IllegalArgumentException) {
                            logger.warn(
                                "Invalid image filename ${orderItem.generatedImageFilename} " +
                                        "for order ${orderData.orderNumber}, using placeholder",
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
            val pdfImage = PDImageXObject.createFromByteArray(document, imageData, PDF_IMAGE_NAME_PRODUCT)

            // Use exact print template dimensions from MugArticleDetails
            // Never scale or correct the image size - use exact dimensions from database
            val imageWidthMm =
                orderItem.article.mugDetails
                    ?.printTemplateWidthMm
                    ?.toFloat()
                    ?: ((pageWidth / MM_TO_POINTS) - (2 * (margin / MM_TO_POINTS)))
            val imageHeightMm =
                orderItem.article.mugDetails
                    ?.printTemplateHeightMm
                    ?.toFloat()
                    ?: ((pageHeight / MM_TO_POINTS) - (2 * (margin / MM_TO_POINTS)) - DEFAULT_IMAGE_MARGIN_MM)

            // Convert exact dimensions to points (no scaling or aspect ratio correction)
            val imageWidthPt = imageWidthMm * MM_TO_POINTS
            val imageHeightPt = imageHeightMm * MM_TO_POINTS

            // Center the image on the page using exact dimensions
            val xPosition = (pageWidth - imageWidthPt) / 2
            val yPosition = (pageHeight - imageHeightPt) / 2

            // Draw image with exact dimensions from database - no scaling
            contentStream.drawImage(pdfImage, xPosition, yPosition, imageWidthPt, imageHeightPt)

            logger.debug(
                "Added product image with exact dimensions ${imageWidthPt}x$imageHeightPt points " +
                        "at position ($xPosition, $yPosition)",
            )
            logger.debug(
                "Using exact print template dimensions: " +
                        "${imageWidthMm}mm x ${imageHeightMm}mm from MugArticleDetails",
            )
        } catch (e: IOException) {
            logger.error("I/O error adding product image for order item ${orderItem.id}", e)
            try {
                // Add placeholder text instead
                addPlaceholderText(contentStream, "Image not available", pageWidth, pageHeight)
            } catch (placeholderException: IOException) {
                logger.error("Failed to add placeholder text for order item ${orderItem.id}", placeholderException)
                throw PdfGenerationException(
                    "Failed to add product image and placeholder for order item ${orderItem.id}",
                    e,
                )
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid image data for order item ${orderItem.id}", e)
            try {
                // Add placeholder text instead
                addPlaceholderText(contentStream, "Image not available", pageWidth, pageHeight)
            } catch (placeholderException: IOException) {
                logger.error("Failed to add placeholder text for order item ${orderItem.id}", placeholderException)
                throw PdfGenerationException(
                    "Failed to add product image and placeholder for order item ${orderItem.id}",
                    e,
                )
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
        margin: Float,
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
        } catch (e: WriterException) {
            logger.error("QR code generation error for order ID $orderId", e)
            try {
                // Add fallback text
                addPlaceholderText(
                    contentStream,
                    "Order ID: $orderId",
                    Float.MAX_VALUE,
                    Float.MAX_VALUE,
                    margin,
                    margin + FALLBACK_TEXT_OFFSET,
                )
            } catch (placeholderException: IOException) {
                logger.error("Failed to add QR code fallback text for order ID $orderId", placeholderException)
                throw PdfGenerationException("Failed to add QR code and fallback text for order ID $orderId", e)
            }
        } catch (e: IOException) {
            logger.error("I/O error generating QR code for order ID $orderId", e)
            try {
                // Add fallback text
                addPlaceholderText(
                    contentStream,
                    "Order ID: $orderId",
                    Float.MAX_VALUE,
                    Float.MAX_VALUE,
                    margin,
                    margin + FALLBACK_TEXT_OFFSET,
                )
            } catch (placeholderException: IOException) {
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
        } catch (e: IOException) {
            throw PdfGenerationException("Failed to create placeholder image", e)
        }
    }

    /**
     * Adds placeholder text when image cannot be rendered
     */
    private fun addPlaceholderText(
        contentStream: PDPageContentStream,
        text: String,
        pageWidth: Float,
        pageHeight: Float,
        x: Float = pageWidth / 2,
        y: Float = pageHeight / 2,
    ) {
        try {
            contentStream.beginText()
            val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            contentStream.setFont(regularFont, PLACEHOLDER_FONT_SIZE)

            if (x == pageWidth / 2) {
                // Center the text
                val textWidth = regularFont.getStringWidth(text) / FONT_WIDTH_DIVISOR * PLACEHOLDER_FONT_SIZE
                contentStream.newLineAtOffset((pageWidth - textWidth) / 2, y)
            } else {
                contentStream.newLineAtOffset(x, y)
            }

            contentStream.showText(text)
            contentStream.endText()
        } catch (e: IOException) {
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
