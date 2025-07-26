package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugVariantService(
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: MugArticleVariantRepository,
    private val imageService: ImageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MugVariantService::class.java)
    }

    @Transactional
    fun create(
        articleId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto {
        val article =
            articleRepository.findById(articleId).orElseGet(null)
                ?: throw ResourceNotFoundException("Article not found with id: $articleId")

        // Check if this should be the default variant
        val existingVariants = mugVariantRepository.findByArticleId(articleId)
        val shouldBeDefault = request.isDefault || existingVariants.isEmpty()

        // If this should be the default, unset any existing default
        if (shouldBeDefault && existingVariants.isNotEmpty()) {
            mugVariantRepository.unsetDefaultForArticle(articleId)
        }

        val variant =
            MugArticleVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageFilename = request.exampleImageFilename,
                supplierArticleNumber = request.supplierArticleNumber,
                isDefault = shouldBeDefault,
            )

        return mugVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElseGet(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        // If this should be the default, unset any existing default
        if (request.isDefault && !variant.isDefault) {
            mugVariantRepository.unsetDefaultForArticle(variant.article.id!!)
        }

        variant.apply {
            insideColorCode = request.insideColorCode
            outsideColorCode = request.outsideColorCode
            name = request.name
            exampleImageFilename = request.exampleImageFilename
            supplierArticleNumber = request.supplierArticleNumber
            isDefault = request.isDefault
        }

        return mugVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun delete(variantId: Long) {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElseGet(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        val articleId = variant.article.id!!
        val wasDefault = variant.isDefault

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.MUG_VARIANT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete mug variant example image during variant deletion: $filename", e)
            }
        }

        mugVariantRepository.deleteById(variantId)

        // If we deleted the default variant, assign default to another variant
        if (wasDefault) {
            val remainingVariants = mugVariantRepository.findByArticleId(articleId)
            if (remainingVariants.isNotEmpty()) {
                val newDefault = remainingVariants.first()
                newDefault.isDefault = true
                mugVariantRepository.save(newDefault)
            }
        }
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<MugArticleVariantDto> =
        mugVariantRepository.findByArticleIdWithArticle(articleId).map(MugArticleVariant::toDto)

    @Transactional
    fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElseGet(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        variant.exampleImageFilename = filename
        return mugVariantRepository.save(variant).toDto()
    }
}
