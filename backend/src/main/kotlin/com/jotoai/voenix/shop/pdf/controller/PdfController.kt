package com.jotoai.voenix.shop.pdf.controller

import com.jotoai.voenix.shop.pdf.service.PdfService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/pdf")
@Validated
class PdfController(
    private val pdfService: PdfService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PdfController::class.java)
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generatePdf(
        @RequestParam("qrContent") @NotBlank(message = "QR content is required") qrContent: String,
        @RequestParam("image") image: MultipartFile,
        @RequestParam("imageWidth") @Positive(message = "Image width must be positive") imageWidth: Float,
        @RequestParam("imageHeight") @Positive(message = "Image height must be positive") imageHeight: Float,
    ): ResponseEntity<ByteArray> {
        logger.info(
            "Generating PDF with QR content: {}, image: {}, dimensions: {}x{} mm",
            qrContent,
            image.originalFilename,
            imageWidth,
            imageHeight,
        )

        if (image.isEmpty) {
            logger.error("Image file is empty")
            return ResponseEntity.badRequest().build()
        }

        val imageData = image.bytes
        val pdfData = pdfService.generatePdf(qrContent, imageData, imageWidth, imageHeight)

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "generated_$timestamp.pdf"

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.size.toLong())
            .body(pdfData)
    }
}
