package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.dto.ArticleMugVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleMugVariantRequest
import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleMugVariantRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugVariantService(
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: ArticleMugVariantRepository,
) {
    @Transactional
    fun create(
        articleId: Long,
        request: CreateArticleMugVariantRequest,
    ): ArticleMugVariantDto {
        val article =
            articleRepository.findById(articleId).orElseGet(null)
                ?: throw ResourceNotFoundException("Article not found with id: $articleId")

        val variant =
            ArticleMugVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageFilename = request.exampleImageFilename,
            )

        return mugVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateArticleMugVariantRequest,
    ): ArticleMugVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElseGet(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        variant.apply {
            insideColorCode = request.insideColorCode
            outsideColorCode = request.outsideColorCode
            name = request.name
            exampleImageFilename = request.exampleImageFilename
        }

        return mugVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun delete(variantId: Long) {
        mugVariantRepository.findById(variantId).orElseGet(null)
            ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")
        mugVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<ArticleMugVariantDto> =
        mugVariantRepository.findByArticleIdWithArticle(articleId).map(ArticleMugVariant::toDto)
}
