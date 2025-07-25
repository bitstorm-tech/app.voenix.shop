package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.dto.ArticlePillowVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticlePillowVariantRequest
import com.jotoai.voenix.shop.domain.articles.entity.ArticlePillowVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticlePillowVariantRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PillowVariantService(
    private val articleRepository: ArticleRepository,
    private val pillowVariantRepository: ArticlePillowVariantRepository,
) {
    @Transactional
    fun create(
        articleId: Long,
        request: CreateArticlePillowVariantRequest,
    ): ArticlePillowVariantDto {
        val article =
            articleRepository
                .findById(articleId)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

        val variant =
            ArticlePillowVariant(
                article = article,
                color = request.color,
                material = request.material,
                exampleImageFilename = request.exampleImageFilename,
            )

        return pillowVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateArticlePillowVariantRequest,
    ): ArticlePillowVariantDto {
        val variant =
            pillowVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Pillow variant not found with id: $variantId") }

        variant.apply {
            color = request.color
            material = request.material
            exampleImageFilename = request.exampleImageFilename
        }

        return pillowVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun delete(variantId: Long) {
        if (!pillowVariantRepository.existsById(variantId)) {
            throw ResourceNotFoundException("Pillow variant not found with id: $variantId")
        }
        pillowVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<ArticlePillowVariantDto> =
        pillowVariantRepository.findByArticleId(articleId).map { it.toDto() }
}
