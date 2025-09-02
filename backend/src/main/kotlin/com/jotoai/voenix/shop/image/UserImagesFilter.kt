package com.jotoai.voenix.shop.image

/**
 * Filter criteria for retrieving user images.
 * Used to specify pagination, filtering, and sorting parameters for user image queries.
 */
data class UserImagesFilter(
    val userId: Long,
    val page: Int = 0,
    val size: Int = 20,
    val type: String? = null, // "all", "uploaded", "generated"
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
)
