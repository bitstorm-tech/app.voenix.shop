package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.dto.ArticleShirtVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleShirtVariantRequest
import com.jotoai.voenix.shop.domain.articles.entity.ArticleShirtVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleShirtVariantRepository
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShirtVariantService(
    private val articleRepository: ArticleRepository,
    private val shirtVariantRepository: ArticleShirtVariantRepository,
    private val imageService: ImageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ShirtVariantService::class.java)
    }

    @Transactional
    fun create(
        articleId: Long,
        request: CreateArticleShirtVariantRequest,
    ): ArticleShirtVariantDto {
        val article =
            articleRepository
                .findById(articleId)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

        val variant =
            ArticleShirtVariant(
                article = article,
                color = request.color,
                size = request.size,
                exampleImageFilename = request.exampleImageFilename,
            )

        return shirtVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateArticleShirtVariantRequest,
    ): ArticleShirtVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Shirt variant not found with id: $variantId") }

        variant.apply {
            color = request.color
            size = request.size
            exampleImageFilename = request.exampleImageFilename
        }

        return shirtVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun delete(variantId: Long) {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Shirt variant not found with id: $variantId") }

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.SHIRT_VARIANT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete shirt variant example image during variant deletion: $filename", e)
            }
        }

        shirtVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<ArticleShirtVariantDto> =
        shirtVariantRepository.findByArticleId(articleId).map { it.toDto() }

    @Transactional
    fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): ArticleShirtVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Shirt variant not found with id: $variantId") }

        variant.exampleImageFilename = filename
        return shirtVariantRepository.save(variant).toDto()
    }
}
