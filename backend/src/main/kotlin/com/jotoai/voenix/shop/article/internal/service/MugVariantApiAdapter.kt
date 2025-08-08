package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.variants.MugVariantFacade
import com.jotoai.voenix.shop.article.api.variants.MugVariantQueryService
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.service.MugVariantService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MugVariantApiAdapter(
    private val delegate: MugVariantService,
) : MugVariantQueryService, MugVariantFacade {

    override fun findByArticleId(articleId: Long): List<MugArticleVariantDto> =
        delegate.findByArticleId(articleId)

    @Transactional
    override fun create(articleId: Long, request: CreateMugArticleVariantRequest): MugArticleVariantDto =
        delegate.create(articleId, request)

    @Transactional
    override fun update(variantId: Long, request: CreateMugArticleVariantRequest): MugArticleVariantDto =
        delegate.update(variantId, request)

    @Transactional
    override fun delete(variantId: Long) {
        delegate.delete(variantId)
    }

    @Transactional
    override fun updateExampleImage(variantId: Long, filename: String): MugArticleVariantDto =
        delegate.updateExampleImage(variantId, filename)

    @Transactional
    override fun removeExampleImage(variantId: Long): MugArticleVariantDto =
        delegate.removeExampleImage(variantId)
}
