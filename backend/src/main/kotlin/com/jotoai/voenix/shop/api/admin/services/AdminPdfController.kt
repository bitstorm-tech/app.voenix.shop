/*
 * PDF generation functionality is temporarily disabled due to memory and performance issues.
 * This feature will be reintroduced in a future update with improved implementation.
 * The original code is preserved below for future reactivation.
 */

package com.jotoai.voenix.shop.api.admin.services

import com.jotoai.voenix.shop.pdf.api.PdfFacade
import com.jotoai.voenix.shop.pdf.api.dto.GeneratePdfRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// Temporarily disabled imports for future reactivation:
// import org.springframework.core.io.ByteArrayResource
// import org.springframework.http.HttpHeaders
// import org.springframework.http.MediaType

@Deprecated("PDF generation is temporarily disabled due to performance issues")
@RestController
@RequestMapping("/api/admin/pdf")
@PreAuthorize("hasRole('ADMIN')")
class AdminPdfController(
    private val pdfFacade: PdfFacade,
) {
    @PostMapping("/generate")
    fun generatePdf(
        @Valid @RequestBody request: GeneratePdfRequest,
    ): ResponseEntity<Map<String, String>> =
        ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(mapOf("message" to "PDF generation is temporarily unavailable. This feature will be reintroduced in a future update."))

    /*
     * Original implementation preserved for future reactivation:
     *
     * @PostMapping("/generate")
     * fun generatePdf(
     *     @Valid @RequestBody request: GeneratePdfRequest,
     * ): ResponseEntity<ByteArrayResource> {
     *     val pdfData = pdfFacade.generatePdf(request)
     *     val resource = ByteArrayResource(pdfData)
     *     val filename = "generated-${System.currentTimeMillis()}.pdf"
     *
     *     return ResponseEntity
     *         .ok()
     *         .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
     *         .contentType(MediaType.APPLICATION_PDF)
     *         .contentLength(pdfData.size.toLong())
     *         .body(resource)
     * }
     */
}
