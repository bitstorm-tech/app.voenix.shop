package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateArticleRequest

/**
 * Facade for Article module write operations.
 * Transitional PR1 version referencing existing DTOs in domain.articles.
 */
interface ArticleFacade {
    fun create(request: CreateArticleRequest): ArticleWithDetailsDto
    fun update(id: Long, request: UpdateArticleRequest): ArticleWithDetailsDto
    fun delete(id: Long)
}
