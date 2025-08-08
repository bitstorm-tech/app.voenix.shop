package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.ArticleFacade
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.CreateArticleRequest
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * PR1 adapter that exposes the new Article APIs while delegating to the existing domain service.
 */
@Service
@Transactional(readOnly = true)
class ArticleApiAdapter(
    private val delegate: ArticleService,
) : ArticleQueryService,
    ArticleFacade {
    override fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType?,
        categoryId: Long?,
        subcategoryId: Long?,
        active: Boolean?,
    ): PaginatedResponse<ArticleDto> = delegate.findAll(page, size, articleType, categoryId, subcategoryId, active)

    override fun findById(id: Long): ArticleWithDetailsDto = delegate.findById(id)

    override fun findPublicMugs(): List<PublicMugDto> = delegate.findPublicMugs()

    @Transactional
    override fun create(request: CreateArticleRequest): ArticleWithDetailsDto = delegate.create(request)

    @Transactional
    override fun update(
        id: Long,
        request: UpdateArticleRequest,
    ): ArticleWithDetailsDto = delegate.update(id, request)

    @Transactional
    override fun delete(id: Long) {
        delegate.delete(id)
    }
}
