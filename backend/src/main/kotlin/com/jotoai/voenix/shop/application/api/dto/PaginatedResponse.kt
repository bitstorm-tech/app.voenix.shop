package com.jotoai.voenix.shop.application.api.dto

/**
 * Generic paginated response wrapper for API endpoints.
 * Provides consistent pagination metadata across all modules.
 */
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean,
)
