package com.jotoai.voenix.shop.pdf.controller

import com.jotoai.voenix.shop.pdf.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.pdf.service.PdfService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @PostMapping("/generate")
    fun generatePdf(
        @Valid @RequestBody request: GeneratePdfRequest,
    ): ResponseEntity<ByteArray> {
        logger.info(
            "Generating PDF for mug ID: {} and imageFilename: {}",
            request.mugId,
            request.imageFilename,
        )

        val pdfData = pdfService.generatePdf(request)

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
