package com.jotoai.voenix.shop.api.public.services

import com.jotoai.voenix.shop.domain.pdf.dto.GeneratePdfRequest
import com.jotoai.voenix.shop.domain.pdf.service.PdfService
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
    private val pdfService: PdfService,
) {
    @PostMapping("/generate")
    fun generatePdf(
        @Valid @RequestBody request: GeneratePdfRequest,
    ): ResponseEntity<ByteArrayResource> {
        val pdfData = pdfService.generatePdf(request)
        val resource = ByteArrayResource(pdfData)
        val filename = "generated-${System.currentTimeMillis()}.pdf"

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.APPLICATION_PDF)
            .contentLength(pdfData.size.toLong())
            .body(resource)
    }
}
