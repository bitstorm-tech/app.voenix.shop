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
            articleRepository
                .findById(articleId)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

        // Validate SKU uniqueness if provided
        request.sku?.let {
            if (mugVariantRepository.existsBySku(it)) {
                throw IllegalArgumentException("SKU already exists: $it")
            }
        }

        val variant =
            ArticleMugVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                sku = request.sku,
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
            mugVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Mug variant not found with id: $variantId") }

        // Validate SKU uniqueness if provided
        request.sku?.let {
            if (mugVariantRepository.existsBySkuAndIdNot(it, variantId)) {
                throw IllegalArgumentException("SKU already exists: $it")
            }
        }

        variant.apply {
            insideColorCode = request.insideColorCode
            outsideColorCode = request.outsideColorCode
            name = request.name
            sku = request.sku
            exampleImageFilename = request.exampleImageFilename
        }

        return mugVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun delete(variantId: Long) {
        if (!mugVariantRepository.existsById(variantId)) {
            throw ResourceNotFoundException("Mug variant not found with id: $variantId")
        }
        mugVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<ArticleMugVariantDto> = mugVariantRepository.findByArticleId(articleId).map { it.toDto() }
}
