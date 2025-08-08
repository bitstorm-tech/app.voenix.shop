package com.jotoai.voenix.shop.article.api.exception

/**
 * Article module specific exception to avoid dependency on common module
 */
class ArticleNotFoundException(
    message: String,
) : RuntimeException(message)
