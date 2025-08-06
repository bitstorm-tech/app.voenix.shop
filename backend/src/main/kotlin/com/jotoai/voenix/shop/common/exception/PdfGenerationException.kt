package com.jotoai.voenix.shop.common.exception

/**
 * Exception thrown when PDF generation fails.
 * This exception wraps various PDF generation errors and provides context about the failure.
 */
class PdfGenerationException : RuntimeException {
    /**
     * Creates a new PdfGenerationException with the specified message.
     *
     * @param message the detail message
     */
    constructor(message: String) : super(message)

    /**
     * Creates a new PdfGenerationException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    constructor(message: String, cause: Throwable) : super(message, cause)

    /**
     * Creates a new PdfGenerationException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    constructor(cause: Throwable) : super(cause)
}
