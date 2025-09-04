package com.jotoai.voenix.shop.article.internal.exception

/**
 * Article module specific exception to avoid dependency on common module
 */
class ArticleNotFoundException(
    message: String,
) : RuntimeException(message)
