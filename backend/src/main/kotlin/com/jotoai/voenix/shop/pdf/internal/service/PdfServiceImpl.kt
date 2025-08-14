package com.jotoai.voenix.shop.pdf.internal.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.pdf.api.PdfFacade
import com.jotoai.voenix.shop.pdf.api.PdfQueryService
import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.pdf.api.dto.PdfResponse
import com.jotoai.voenix.shop.pdf.api.dto.PdfSize
import com.jotoai.voenix.shop.pdf.api.exceptions.PdfGenerationException
import com.jotoai.voenix.shop.pdf.internal.config.PdfQrProperties
import jakarta.annotation.PostConstruct
import com.lowagie.text.Document
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

/**
 * PDF generation service implementation using OpenPDF library.
 * Migrated from Apache PDFBox for improved memory efficiency and performance.
 */

@Service
class PdfServiceImpl(
    @param:Value("\${app.base-url}") private val appBaseUrl: String,
    private val articleQueryService: ArticleQueryService,
    private val storagePathService: StoragePathService,
    private val pdfQrProperties: PdfQrProperties,
) : PdfFacade,
    PdfQueryService {
    companion object {
        private val logger = LoggerFactory.getLogger(PdfServiceImpl::class.java)

        // PDF conversion and layout constants
        private const val MM_TO_POINTS = 2.8346457f
        private const val QR_CODE_SIZE_PIXELS = 150
        private const val QR_CODE_SIZE_POINTS = 150f

        // Image format constants
        private const val IMAGE_FORMAT_PNG = "PNG"
        private const val PDF_IMAGE_NAME_QR = "QRCode"
        private const val PDF_IMAGE_NAME_MAIN = "Image"
    }

    @PostConstruct
    fun init() {
        // Initialize baseUrl from appBaseUrl if not configured
        if (pdfQrProperties.baseUrl.isEmpty()) {
            pdfQrProperties.baseUrl = appBaseUrl
            logger.info("Initialized PDF QR base URL with app base URL: $appBaseUrl")
        }
    }

    override fun generatePdf(request: GeneratePdfRequest): ByteArray {
        try {
            val article = articleQueryService.findById(request.articleId)

            val mugDetails =
                article.mugDetails
                    ?: throw IllegalArgumentException("Article ${request.articleId} is not a mug or has no mug details")

            // Load image data using the filename and StoragePathService
            val imageData =
                try {
                    val imageType =
                        storagePathService.findImageTypeByFilename(request.imageFilename)
                            ?: throw PdfGenerationException(
                                "Could not determine image type for filename: ${request.imageFilename}",
                            )
                    val imagePath = storagePathService.getPhysicalFilePath(imageType, request.imageFilename)
                    imagePath.toFile().readBytes()
                } catch (e: IOException) {
                    throw PdfGenerationException("Failed to load image data for filename: ${request.imageFilename}", e)
                } catch (e: IllegalArgumentException) {
                    throw PdfGenerationException("Invalid image filename: ${request.imageFilename}", e)
                }

            // Validate document format fields from database
            val pdfWidthMm =
                mugDetails.documentFormatWidthMm?.toFloat()
                    ?: throw PdfGenerationException("Document format width not configured for article ${request.articleId}")
            val pdfHeightMm =
                mugDetails.documentFormatHeightMm?.toFloat()
                    ?: throw PdfGenerationException("Document format height not configured for article ${request.articleId}")
            val marginMm =
                mugDetails.documentFormatMarginBottomMm?.toFloat()
                    ?: throw PdfGenerationException("Document format margin not configured for article ${request.articleId}")

            // Convert dimensions from mm to points
            val pdfSize =
                PdfSize(
                    width = pdfWidthMm * MM_TO_POINTS,
                    height = pdfHeightMm * MM_TO_POINTS,
                    margin = marginMm * MM_TO_POINTS,
                )

            // Use Document and PdfWriter for OpenPDF
            val outputStream = ByteArrayOutputStream()
            val document = Document(Rectangle(pdfSize.width, pdfSize.height))
            val writer = PdfWriter.getInstance(document, outputStream)
            
            document.open()
            val contentByte = writer.directContent
            
            // Generate QR code URL pointing to the article
            val qrUrl = pdfQrProperties.generateQrUrl("/articles/${request.articleId}")
            addQrCode(contentByte, qrUrl, pdfSize)
            
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
            logger.error("Failed to generate PDF for article ${request.articleId}", e)

            throw e
        } catch (e: IOException) {
            logger.error("I/O error during PDF generation for article ${request.articleId}", e)
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        } catch (e: WriterException) {
            logger.error("QR code generation error during PDF generation for article ${request.articleId}", e)
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid argument during PDF generation for article ${request.articleId}", e)
            throw PdfGenerationException("PDF generation failed for article ${request.articleId}", e)
        }
    }

    override fun getPdfMetadata(filename: String): PdfResponse {
        // Implementation for getting PDF metadata
        // This is a placeholder - in a real implementation, you might store metadata in a database
        return PdfResponse(
            filename = filename,
            size = 0L,
            contentType = "application/pdf",
        )
    }

    override fun pdfExists(filename: String): Boolean {
        // Implementation for checking if PDF exists
        // This is a placeholder - in a real implementation, you might check file system or database
        return false
    }

    private fun addQrCode(
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
            
            val qrX = pdfSize.margin
            val qrY = pdfSize.height - pdfSize.margin - QR_CODE_SIZE_POINTS
            
            qrImage.setAbsolutePosition(qrX, qrY)
            contentByte.addImage(qrImage)
            logger.debug("QR code placed at position ({}, {})", qrX, qrY)
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
            logger.debug(
                "Image placed at position ({}, {}) with size {}x{} points",
                x,
                y,
                imageWidthPoints,
                imageHeightPoints,
            )
        } catch (e: IOException) {
            throw PdfGenerationException("I/O error adding centered image to PDF", e)
        } catch (e: IllegalArgumentException) {
            throw PdfGenerationException("Invalid image data for centered image", e)
        }
    }
}
