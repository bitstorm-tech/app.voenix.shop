package com.jotoai.voenix.shop.common.dto

data class PaginatedResponse<T>(
    val content: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
)
