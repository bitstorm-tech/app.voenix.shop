/*
 * PDF generation functionality is temporarily disabled due to memory and performance issues.
 * This service is preserved for future reactivation when improved implementation is ready.
 * Controllers now return HTTP 503 Service Unavailable instead of calling these services.
 */

package com.jotoai.voenix.shop.domain.pdf.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import com.jotoai.voenix.shop.domain.images.service.ImageService
import com.jotoai.voenix.shop.domain.pdf.config.PdfQrProperties
import com.jotoai.voenix.shop.domain.pdf.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.domain.pdf.dto.PdfSize
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class PdfService(
    @param:Value("\${pdf.size.width}") private val pdfWidthMm: Float,
    @param:Value("\${pdf.size.height}") private val pdfHeightMm: Float,
    @param:Value("\${pdf.margin}") private val marginMm: Float,
    @param:Value("\${app.base-url}") private val appBaseUrl: String,
    private val articleService: ArticleService,
    private val imageService: ImageService,
    private val pdfQrProperties: PdfQrProperties,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PdfService::class.java)
        private const val MM_TO_POINTS = 2.834645669f
        private const val QR_CODE_SIZE = 150
    }

    private val pdfSize =
        PdfSize(
            width = pdfWidthMm * MM_TO_POINTS,
            height = pdfHeightMm * MM_TO_POINTS,
            margin = marginMm * MM_TO_POINTS,
        )

    fun generatePdf(request: GeneratePdfRequest): ByteArray {
        val article = articleService.findById(request.articleId)

        val mugDetails =
            article.mugDetails
                ?: throw IllegalArgumentException("Article ${request.articleId} is not a mug or has no mug details")

        // Load image data using the filename
        val (imageData, _) = imageService.getImageData(request.imageFilename)

        // Initialize PDF QR properties with app base URL if not configured
        if (pdfQrProperties.baseUrl.isEmpty()) {
            pdfQrProperties.baseUrl = appBaseUrl
        }

        val document = PDDocument()
        return try {
            val page = PDPage(PDRectangle(pdfSize.width, pdfSize.height))
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                // Generate QR code URL pointing to the article
                val qrUrl = pdfQrProperties.generateQrUrl("/articles/${request.articleId}")
                addQrCode(document, contentStream, qrUrl)

                addCenteredImage(
                    document,
                    contentStream,
                    imageData,
                    mugDetails.printTemplateWidthMm.toFloat(),
                    mugDetails.printTemplateHeightMm.toFloat(),
                )
            }

            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)
            outputStream.toByteArray()
        } finally {
            document.close()
        }
    }

    private fun addQrCode(
        document: PDDocument,
        contentStream: PDPageContentStream,
        qrContent: String,
    ) {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE)

        val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
        val qrByteArray = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "PNG", qrByteArray)

        val qrImage = PDImageXObject.createFromByteArray(document, qrByteArray.toByteArray(), "QRCode")

        val qrX = pdfSize.margin
        val qrY = pdfSize.height - pdfSize.margin - QR_CODE_SIZE

        contentStream.drawImage(qrImage, qrX, qrY, QR_CODE_SIZE.toFloat(), QR_CODE_SIZE.toFloat())
        logger.debug("QR code placed at position ({}, {})", qrX, qrY)
    }

    private fun addCenteredImage(
        document: PDDocument,
        contentStream: PDPageContentStream,
        imageData: ByteArray,
        imageWidthMm: Float,
        imageHeightMm: Float,
    ) {
        val image = PDImageXObject.createFromByteArray(document, imageData, "Image")

        val imageWidthPoints = imageWidthMm * MM_TO_POINTS
        val imageHeightPoints = imageHeightMm * MM_TO_POINTS

        val x = (pdfSize.width - imageWidthPoints) / 2
        val y = (pdfSize.height - imageHeightPoints) / 2

        contentStream.drawImage(image, x, y, imageWidthPoints, imageHeightPoints)
        logger.debug("Image placed at position ({}, {}) with size {}x{} points", x, y, imageWidthPoints, imageHeightPoints)
    }
}
