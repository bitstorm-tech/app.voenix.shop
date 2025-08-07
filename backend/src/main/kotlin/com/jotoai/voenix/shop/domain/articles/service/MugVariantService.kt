package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugVariantService(
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: MugArticleVariantRepository,
    private val imageService: ImageService,
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
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
            articleRepository.findById(articleId).orElse(null)
                ?: throw ResourceNotFoundException("Article not found with id: $articleId")

        // Check if this should be the default variant
        val existingVariants = mugVariantRepository.findByArticleId(articleId)
        val shouldBeDefault = request.isDefault || existingVariants.isEmpty()

        // If this should be the default, unset any existing default
        if (shouldBeDefault) {
            mugVariantRepository.unsetDefaultForArticle(articleId)
        }

        val variant =
            MugArticleVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = shouldBeDefault,
            )

        val savedVariant = mugVariantRepository.save(variant)
        logger.debug("Created mug variant ${savedVariant.id} for article $articleId with isDefault=$shouldBeDefault")

        return mugArticleVariantAssembler.toDto(savedVariant)
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        val articleId = variant.article.id!!
        val wasDefault = variant.isDefault

        // If this should be the default, unset any existing default (including this one if it was default)
        if (request.isDefault) {
            mugVariantRepository.unsetDefaultForArticle(articleId)
        }

        // If we're unsetting this as default and it was the default, ensure at least one variant is default
        if (!request.isDefault && wasDefault) {
            val otherVariants =
                mugVariantRepository
                    .findByArticleId(articleId)
                    .filter { it.id != variantId }

            if (otherVariants.isNotEmpty()) {
                // Make the first other variant the default
                val newDefault = otherVariants.first()
                newDefault.isDefault = true
                mugVariantRepository.save(newDefault)
                logger.debug("Assigned default to variant ${newDefault.id} as variant $variantId is no longer default")
            }
        }

        variant.apply {
            insideColorCode = request.insideColorCode
            outsideColorCode = request.outsideColorCode
            name = request.name
            articleVariantNumber = request.articleVariantNumber
            isDefault = request.isDefault
        }

        val savedVariant = mugVariantRepository.save(variant)
        logger.debug("Updated mug variant $variantId with isDefault=${request.isDefault}")

        return mugArticleVariantAssembler.toDto(savedVariant)
    }

    @Transactional
    fun delete(variantId: Long) {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
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
        mugVariantRepository.findByArticleIdWithArticle(articleId).map { mugArticleVariantAssembler.toDto(it) }

    @Transactional
    fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        variant.exampleImageFilename = filename
        return mugArticleVariantAssembler.toDto(mugVariantRepository.save(variant))
    }

    @Transactional
    fun removeExampleImage(variantId: Long): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        // Delete the image file if it exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.MUG_VARIANT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete mug variant example image: $filename", e)
            }
        }

        // Clear the filename in the database
        variant.exampleImageFilename = null
        return mugArticleVariantAssembler.toDto(mugVariantRepository.save(variant))
    }
}
