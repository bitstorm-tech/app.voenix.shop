package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.internal.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugVariantServiceImpl(
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: MugArticleVariantRepository,
    private val imageStorageService: ImageStorageService,
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
) : com.jotoai.voenix.shop.article.api.variants.MugVariantQueryService,
    com.jotoai.voenix.shop.article.api.variants.MugVariantFacade {
    companion object {
        private val logger = LoggerFactory.getLogger(MugVariantServiceImpl::class.java)
    }

    @Transactional
    override fun create(
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
    override fun update(
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
    override fun delete(variantId: Long) {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        val articleId = variant.article.id!!
        val wasDefault = variant.isDefault

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteFile(filename, ImageType.MUG_VARIANT_EXAMPLE)
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
    override fun findByArticleId(articleId: Long): List<MugArticleVariantDto> =
        mugVariantRepository.findByArticleIdWithArticle(articleId).map { mugArticleVariantAssembler.toDto(it) }

    @Transactional
    override fun updateExampleImage(
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
    override fun removeExampleImage(variantId: Long): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ResourceNotFoundException("Mug variant not found with id: $variantId")

        // Delete the image file if it exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteFile(filename, ImageType.MUG_VARIANT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete mug variant example image: $filename", e)
            }
        }

        // Clear the filename in the database
        variant.exampleImageFilename = null
        return mugArticleVariantAssembler.toDto(mugVariantRepository.save(variant))
    }
}
