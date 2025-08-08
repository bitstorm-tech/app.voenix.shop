package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.variants.ShirtVariantFacade
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantQueryService
import com.jotoai.voenix.shop.domain.articles.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.service.ShirtVariantService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ShirtVariantApiAdapter(
    private val delegate: ShirtVariantService,
) : ShirtVariantQueryService, ShirtVariantFacade {

    override fun findByArticleId(articleId: Long): List<ShirtArticleVariantDto> =
        delegate.findByArticleId(articleId)

    @Transactional
    override fun create(articleId: Long, request: CreateShirtArticleVariantRequest): ShirtArticleVariantDto =
        delegate.create(articleId, request)

    @Transactional
    override fun update(variantId: Long, request: CreateShirtArticleVariantRequest): ShirtArticleVariantDto =
        delegate.update(variantId, request)

    @Transactional
    override fun delete(variantId: Long) {
        delegate.delete(variantId)
    }

    @Transactional
    override fun updateExampleImage(variantId: Long, filename: String): ShirtArticleVariantDto =
        delegate.updateExampleImage(variantId, filename)
}
