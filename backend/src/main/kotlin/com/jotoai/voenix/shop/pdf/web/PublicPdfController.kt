/**
 * Public PDF generation controller using OpenPDF library.
 * Migrated from Apache PDFBox for improved memory efficiency and performance.
 */

package com.jotoai.voenix.shop.pdf.web
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.api.dto.PublicPdfGenerationRequest
import jakarta.validation.Valid
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/pdf")
class PublicPdfController(
    private val pdfGenerationService: PdfGenerationService,
) {
    @PostMapping("/generate")
    fun generatePdf(
        @Valid @RequestBody request: PublicPdfGenerationRequest,
    ): ResponseEntity<ByteArrayResource> {
        val pdfData = pdfGenerationService.generatePublicPdf(request)
        val resource = ByteArrayResource(pdfData)
        val filename = "mug-preview-${System.currentTimeMillis()}.pdf"

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.size.toLong())
            .body(resource)
    }
}
