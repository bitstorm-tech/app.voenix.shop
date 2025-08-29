package com.jotoai.voenix.shop.application.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * Standard error response DTO for API endpoints.
 * Provides consistent error messaging across all modules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: Map<String, String>? = null,
)
