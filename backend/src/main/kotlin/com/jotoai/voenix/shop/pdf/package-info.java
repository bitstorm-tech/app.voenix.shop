/**
 * PDF generation module for creating printable documents.
 * <p>
 * This module provides:
 * <ul>
 *   <li>PDF generation operations through {@link com.jotoai.voenix.shop.pdf.api.PdfFacade}</li>
 *   <li>PDF query operations and metadata access through {@link com.jotoai.voenix.shop.pdf.api.PdfQueryService}</li>
 *   <li>Order-specific PDF generation through {@link com.jotoai.voenix.shop.pdf.api.OrderPdfService}</li>
 *   <li>Public PDF generation through {@link com.jotoai.voenix.shop.pdf.api.PublicPdfService}</li>
 * </ul>
 * <p>
 * Named interfaces:
 * <ul>
 *   <li>{@code api} - Public API for PDF operations and queries</li>
 *   <li>{@code web} - REST controllers for PDF endpoints</li>
 * </ul>
 * <p>
 * Other modules should explicitly depend on {@code pdf::api} to access PDF functionality.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "PDF Generation"
)
package com.jotoai.voenix.shop.pdf;