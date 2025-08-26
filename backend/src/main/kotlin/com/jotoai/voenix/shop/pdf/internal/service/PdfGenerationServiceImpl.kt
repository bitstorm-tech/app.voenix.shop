package com.jotoai.voenix.shop.pdf.internal.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.pdf.api.dto.OrderItemPdfData
import com.jotoai.voenix.shop.pdf.api.dto.OrderPdfData
import com.jotoai.voenix.shop.pdf.api.dto.PdfSize
import com.jotoai.voenix.shop.pdf.api.dto.PublicPdfGenerationRequest
import com.jotoai.voenix.shop.pdf.api.exceptions.PdfGenerationException
import com.jotoai.voenix.shop.pdf.internal.config.PdfQrProperties
import com.lowagie.text.Document
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Consolidated PDF generation service implementation using OpenPDF library.
 * This service replaces and consolidates functionality from:
 * - PdfServiceImpl (PdfFacade, PdfQueryService)
 * - OrderPdfServiceImpl (OrderPdfService)
 * - PublicPdfServiceImpl (PublicPdfService)
 *
 * Benefits of consolidation:
 * - Shared utility methods for QR generation, document creation, and image handling
 * - Consistent error handling across all PDF operations
 * - Simplified dependency injection and testing
 * - Reduced code duplication
 *
 * Migrated from Apache PDFBox to OpenPDF for improved memory efficiency and performance.
 */
@Service
@Transactional(readOnly = true)
class PdfGenerationServiceImpl(
    @param:Value("\${app.base-url}") private val appBaseUrl: String,
    @param:Value("\${pdf.size.width:239}") private val defaultPdfWidthMm: Float,
    @param:Value("\${pdf.size.height:99}") private val defaultPdfHeightMm: Float,
    @param:Value("\${pdf.margin:1}") private val defaultPdfMarginMm: Float,
    private val articleQueryService: ArticleQueryService,
    private val storagePathService: StoragePathService,
    private val imageAccessService: ImageAccessService,
    private val pdfQrProperties: PdfQrProperties,
    private val orderDataConverter: OrderDataConverter,
) : PdfGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}

        // PDF conversion and layout constants
        private const val MM_TO_POINTS = 2.8346457f

        // QR code settings
        private const val QR_CODE_SIZE_PIXELS = 150
        private const val QR_CODE_SIZE_POINTS = 150f
        private const val ORDER_QR_SIZE_PIXELS = 100
        private const val ORDER_QR_CODE_SIZE_POINTS = 40f

        // Image format constants
        private const val IMAGE_FORMAT_PNG = "PNG"

        // Order PDF layout constants - removed unused positioning constants

        // Font settings
        private const val HEADER_FONT_SIZE = 14f
        private const val PLACEHOLDER_FONT_SIZE = 12f

        // Placeholder image settings
        private const val PLACEHOLDER_IMAGE_WIDTH = 400
        private const val PLACEHOLDER_IMAGE_HEIGHT = 300
        private const val PLACEHOLDER_FONT_SIZE_PIXELS = 24

        // Default image margins when mug details are not available
        private const val DEFAULT_IMAGE_MARGIN_MM = 15f

        // Text positioning
        private const val FALLBACK_TEXT_OFFSET = 10f
        private const val LEFT_HEADER_X_OFFSET = 15f
        private const val RIGHT_INFO_X_OFFSET = 5f

        // Text rotation constants
        private const val TEXT_ROTATION_VERTICAL = 90f
    }

    @PostConstruct
    fun init() {
        // Initialize baseUrl from appBaseUrl if not configured
        if (pdfQrProperties.baseUrl.isEmpty()) {
            pdfQrProperties.baseUrl = appBaseUrl
            logger.info { "Initialized PDF QR base URL with app base URL: $appBaseUrl" }
        }
    }

    override fun generatePdf(request: GeneratePdfRequest): ByteArray {
        try {
            val article = articleQueryService.findById(request.articleId)

            val mugDetails =
                article.mugDetails
                    ?: throw IllegalArgumentException("Article ${request.articleId} is not a mug or has no mug details")

            // Load image data using the filename and StoragePathService
            val imageData = loadImageData(request.imageFilename)

            // Validate document format fields from database
            val pdfSize =
                createPdfSize(
                    mugDetails.documentFormatWidthMm,
                    mugDetails.documentFormatHeightMm,
                    mugDetails.documentFormatMarginBottomMm,
                    request.articleId,
                )

            // Create PDF document
            val outputStream = ByteArrayOutputStream()
            val document = Document(Rectangle(pdfSize.width, pdfSize.height))
            val writer = PdfWriter.getInstance(document, outputStream)

            document.open()
            val contentByte = writer.directContent

            // Generate QR code URL pointing to the article
            val qrUrl = pdfQrProperties.generateQrUrl("/articles/${request.articleId}")
            addArticleQrCode(contentByte, qrUrl, pdfSize)

            addCenteredImage(
                contentByte,
                imageData,
                mugDetails.printTemplateWidthMm.toFloat(),
                mugDetails.printTemplateHeightMm.toFloat(),
                pdfSize,
            )

            document.close()
            return outputStream.toByteArray()
        } catch (e: PdfGenerationException) {
            logger.error(e) { "Failed to generate PDF for article ${request.articleId}" }
            throw e
        } catch (e: IOException) {
            logger.error(e) { "I/O error during PDF generation for article ${request.articleId}" }
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        } catch (e: WriterException) {
            logger.error(e) { "QR code generation error during PDF generation for article ${request.articleId}" }
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid argument during PDF generation for article ${request.articleId}" }
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        }
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

    override fun generatePublicPdf(request: PublicPdfGenerationRequest): ByteArray {
        try {
            val article =
                try {
                    articleQueryService.findById(request.mugId)
                } catch (_: ResourceNotFoundException) {
                    throw BadRequestException("Mug not found or unavailable")
                }

            if (article.mugDetails == null) {
                throw BadRequestException("The specified article is not a mug")
            }

            if (!article.active) {
                throw BadRequestException("This mug is currently unavailable")
            }

            val filename = request.imageUrl.substringAfterLast("/")

            logger.info { "Processing public PDF generation for mug ID: ${request.mugId}" }

            val pdfRequest =
                GeneratePdfRequest(
                    articleId = request.mugId,
                    imageFilename = filename,
                )

            return generatePdf(pdfRequest)
        } catch (e: BadRequestException) {
            logger.error(e) { "Bad request error generating PDF for public user" }
            throw e
        } catch (e: ResourceNotFoundException) {
            logger.error(e) { "Resource not found error generating PDF for public user" }
            throw e
        } catch (e: PdfGenerationException) {
            logger.error(e) { "PDF generation error for public user" }
            throw RuntimeException("Failed to generate PDF. Please try again later.", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid argument error generating PDF for public user" }
            throw BadRequestException("Invalid request parameters")
        } catch (e: IllegalStateException) {
            logger.error(e) { "Invalid state error generating PDF for public user" }
            throw RuntimeException("Service temporarily unavailable. Please try again later.", e)
        }
    }

    private fun loadImageData(imageFilename: String): ByteArray =
        try {
            val imageType =
                storagePathService.findImageTypeByFilename(imageFilename)
                    ?: throw PdfGenerationException("Could not determine image type for filename: $imageFilename")
            val imagePath = storagePathService.getPhysicalFilePath(imageType, imageFilename)
            imagePath.toFile().readBytes()
        } catch (e: IOException) {
            throw PdfGenerationException("Failed to load image data for filename: $imageFilename", e)
        } catch (e: IllegalArgumentException) {
            throw PdfGenerationException("Invalid image filename: $imageFilename", e)
        }

    private fun createPdfSize(
        widthMm: Int?,
        heightMm: Int?,
        marginMm: Int?,
        articleId: Long,
    ): PdfSize {
        val pdfWidthMm =
            widthMm?.toFloat()
                ?: throw PdfGenerationException("Document format width not configured for article $articleId")
        val pdfHeightMm =
            heightMm?.toFloat()
                ?: throw PdfGenerationException("Document format height not configured for article $articleId")
        val marginMmFloat =
            marginMm?.toFloat()
                ?: throw PdfGenerationException("Document format margin not configured for article $articleId")

        return PdfSize(
            width = pdfWidthMm * MM_TO_POINTS,
            height = pdfHeightMm * MM_TO_POINTS,
            margin = marginMmFloat * MM_TO_POINTS,
        )
    }

    private fun addArticleQrCode(
        contentByte: PdfContentByte,
        qrContent: String,
        pdfSize: PdfSize,
    ) {
        try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix =
                qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE_PIXELS,
                    QR_CODE_SIZE_PIXELS,
                )

            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            val qrByteArray = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, IMAGE_FORMAT_PNG, qrByteArray)

            val qrImage = Image.getInstance(qrByteArray.toByteArray())
            qrImage.scaleAbsolute(QR_CODE_SIZE_POINTS, QR_CODE_SIZE_POINTS)

            val qrX = 0f
            val qrY = pdfSize.height - QR_CODE_SIZE_POINTS

            qrImage.setAbsolutePosition(qrX, qrY)
            contentByte.addImage(qrImage)
            logger.debug { "QR code placed at position ($qrX, $qrY)" }
        } catch (e: WriterException) {
            throw PdfGenerationException("Failed to generate QR code for content: $qrContent", e)
        } catch (e: IOException) {
            throw PdfGenerationException("I/O error generating QR code for content: $qrContent", e)
        }
    }

    private fun addCenteredImage(
        contentByte: PdfContentByte,
        imageData: ByteArray,
        imageWidthMm: Float,
        imageHeightMm: Float,
        pdfSize: PdfSize,
    ) {
        try {
            val image = Image.getInstance(imageData)

            val imageWidthPoints = imageWidthMm * MM_TO_POINTS
            val imageHeightPoints = imageHeightMm * MM_TO_POINTS

            image.scaleAbsolute(imageWidthPoints, imageHeightPoints)

            val x = (pdfSize.width - imageWidthPoints) / 2
            val y = (pdfSize.height - imageHeightPoints) / 2

            image.setAbsolutePosition(x, y)
            contentByte.addImage(image)
            logger.debug {
                "Image placed at position ($x, $y) with size ${imageWidthPoints}x$imageHeightPoints points"
            }
        } catch (e: IOException) {
            throw PdfGenerationException("I/O error adding centered image to PDF", e)
        } catch (e: IllegalArgumentException) {
            throw PdfGenerationException("Invalid image data for centered image", e)
        }
    }

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
                itemPageWidth,
                itemPageHeight,
                itemMargin,
            )

            // Add QR code in bottom left
            addOrderQrCode(contentByte, orderData.id.toString(), itemMargin)
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

        logger.debug { "Created page $pageNumber/$totalPages for order ${orderData.orderNumber}" }
    }

    private fun getPageWidth(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatWidthMm
            ?.let { it * MM_TO_POINTS }
            ?: (defaultPdfWidthMm * MM_TO_POINTS)

    private fun getPageHeight(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatHeightMm
            ?.let { it * MM_TO_POINTS }
            ?: (defaultPdfHeightMm * MM_TO_POINTS)

    private fun getMargin(orderItem: OrderItemPdfData): Float =
        orderItem.article.mugDetails
            ?.documentFormatMarginBottomMm
            ?.let { it * MM_TO_POINTS }
            ?: (defaultPdfMarginMm * MM_TO_POINTS)

    private fun addOrderHeader(
        contentByte: PdfContentByte,
        orderNumber: String,
        pageNumber: Int,
        totalPages: Int,
        pageHeight: Float,
    ) {
        val baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        val fontSize = HEADER_FONT_SIZE

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

    private fun addProductImage(
        contentByte: PdfContentByte,
        orderData: OrderPdfData,
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
        margin: Float,
    ) {
        try {
            val imageData = loadProductImage(orderData, orderItem)
            val pdfImage = Image.getInstance(imageData)
            val dimensions = calculateImageDimensions(orderItem, pageWidth, pageHeight, margin)
            
            positionAndAddImage(contentByte, pdfImage, dimensions, pageWidth, pageHeight)
            logImageAddition(dimensions)
        } catch (e: Exception) {
            handleImageAdditionError(e, contentByte, orderItem, pageWidth, pageHeight)
        }
    }
    
    private fun loadProductImage(orderData: OrderPdfData, orderItem: OrderItemPdfData): ByteArray {
        return when {
            orderItem.generatedImageFilename != null -> {
                try {
                    imageAccessService.getImageData(orderItem.generatedImageFilename, orderData.userId).first
                } catch (e: IOException) {
                    logger.warn(e) {
                        "Could not load generated image ${orderItem.generatedImageFilename} " +
                            "for order ${orderData.orderNumber}, using placeholder"
                    }
                    createPlaceholderImage()
                } catch (e: IllegalArgumentException) {
                    logger.warn(e) {
                        "Invalid image filename ${orderItem.generatedImageFilename} " +
                            "for order ${orderData.orderNumber}, using placeholder"
                    }
                    createPlaceholderImage()
                }
            }
            else -> {
                logger.info { "No generated image for order item ${orderItem.id}, using placeholder" }
                createPlaceholderImage()
            }
        }
    }
    
    private fun calculateImageDimensions(
        orderItem: OrderItemPdfData,
        pageWidth: Float,
        pageHeight: Float,
        margin: Float
    ): ImageDimensions {
        val imageWidthMm = orderItem.article.mugDetails
            ?.printTemplateWidthMm
            ?.toFloat()
            ?: ((pageWidth / MM_TO_POINTS) - (2 * (margin / MM_TO_POINTS)))
            
        val imageHeightMm = orderItem.article.mugDetails
            ?.printTemplateHeightMm
            ?.toFloat()
            ?: ((pageHeight / MM_TO_POINTS) - (2 * (margin / MM_TO_POINTS)) - DEFAULT_IMAGE_MARGIN_MM)

        val imageWidthPt = imageWidthMm * MM_TO_POINTS
        val imageHeightPt = imageHeightMm * MM_TO_POINTS
        
        return ImageDimensions(imageWidthPt, imageHeightPt)
    }
    
    private fun positionAndAddImage(
        contentByte: PdfContentByte,
        pdfImage: Image,
        dimensions: ImageDimensions,
        pageWidth: Float,
        pageHeight: Float
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
        pageHeight: Float
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
        originalException: Exception
    ) {
        try {
            addPlaceholderText(contentByte, "Image not available", pageWidth, pageHeight, 0f, 0f)
        } catch (placeholderException: IOException) {
            logger.error(placeholderException) { "Failed to add placeholder text for order item ${orderItem.id}" }
            throw PdfGenerationException(
                "Failed to add product image and placeholder for order item ${orderItem.id}",
                originalException,
            )
        }
    }
    
    private data class ImageDimensions(val width: Float, val height: Float)

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
                    ORDER_QR_SIZE_PIXELS,
                    ORDER_QR_SIZE_PIXELS,
                )

            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            val qrByteArray = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, IMAGE_FORMAT_PNG, qrByteArray)

            val qrImage = Image.getInstance(qrByteArray.toByteArray())
            qrImage.scaleAbsolute(ORDER_QR_CODE_SIZE_POINTS, ORDER_QR_CODE_SIZE_POINTS)

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
                    "Order ID: $orderId",
                    Float.MAX_VALUE,
                    Float.MAX_VALUE,
                    margin,
                    margin + FALLBACK_TEXT_OFFSET,
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
                    "Order ID: $orderId",
                    Float.MAX_VALUE,
                    Float.MAX_VALUE,
                    margin,
                    margin + FALLBACK_TEXT_OFFSET,
                )
            } catch (placeholderException: IOException) {
                logger.error(placeholderException) { "Failed to add QR code fallback text for order ID $orderId" }
                throw PdfGenerationException("Failed to add QR code and fallback text for order ID $orderId", e)
            }
        }
    }

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

    private fun addPlaceholderText(
        contentByte: PdfContentByte,
        text: String,
        pageWidth: Float,
        pageHeight: Float,
        x: Float = pageWidth / 2,
        y: Float = pageHeight / 2,
    ) {
        try {
            val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
            contentByte.beginText()
            contentByte.setFontAndSize(baseFont, PLACEHOLDER_FONT_SIZE)

            if (x == pageWidth / 2) {
                // Center the text
                val textWidth = baseFont.getWidthPoint(text, PLACEHOLDER_FONT_SIZE)
                contentByte.moveText((pageWidth - textWidth) / 2, y)
            } else {
                contentByte.moveText(x, y)
            }

            contentByte.showText(text)
            contentByte.endText()
        } catch (e: IOException) {
            logger.error(e) { "Failed to add placeholder text: $text" }
        }
    }

    override fun convertToOrderPdfData(orderForPdf: OrderForPdfDto) =
        orderDataConverter.convertToOrderPdfData(orderForPdf)
}
