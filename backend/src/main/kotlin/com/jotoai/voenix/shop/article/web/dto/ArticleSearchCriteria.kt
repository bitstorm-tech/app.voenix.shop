package com.jotoai.voenix.shop.article.web.dto

import com.jotoai.voenix.shop.article.api.enums.ArticleType

data class ArticleSearchCriteria(
    val page: Int = 0,
    val size: Int = 20,
    val type: ArticleType? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val active: Boolean? = null,
)
