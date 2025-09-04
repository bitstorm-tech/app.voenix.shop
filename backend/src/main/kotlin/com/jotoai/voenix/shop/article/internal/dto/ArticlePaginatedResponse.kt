package com.jotoai.voenix.shop.article.internal.dto

/**
 * Article module specific paginated response to avoid dependency on common module
 */
data class ArticlePaginatedResponse<T>(
    val content: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val hasNext: Boolean = currentPage < totalPages - 1,
    val hasPrevious: Boolean = currentPage > 0,
)
