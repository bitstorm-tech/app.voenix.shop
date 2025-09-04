package com.jotoai.voenix.shop.article.internal.dto

import com.jotoai.voenix.shop.article.ArticleType

/**
 * Query object for searching articles with pagination and optional filters.
 */
data class FindArticlesQuery(
    val page: Int,
    val size: Int,
    val articleType: ArticleType? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val active: Boolean? = null,
)
