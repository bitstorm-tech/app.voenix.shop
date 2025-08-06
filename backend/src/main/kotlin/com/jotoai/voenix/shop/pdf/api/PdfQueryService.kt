package com.jotoai.voenix.shop.pdf.api

import com.jotoai.voenix.shop.pdf.api.dto.PdfResponse

/**
 * Query service for PDF module read operations.
 * This interface defines all read-only operations for PDF metadata.
 */
interface PdfQueryService {
    /**
     * Gets PDF metadata for a generated PDF.
     * @param filename The PDF filename
     * @return PDF metadata
     */
    fun getPdfMetadata(filename: String): PdfResponse

    /**
     * Validates if a PDF exists and is accessible.
     * @param filename The PDF filename
     * @return true if the PDF exists and is accessible
     */
    fun pdfExists(filename: String): Boolean
}
