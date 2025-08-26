package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.CopyVariantsRequest
import com.jotoai.voenix.shop.article.api.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.MugWithVariantsSummaryDto
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.api.variants.MugVariantFacade
import com.jotoai.voenix.shop.article.api.variants.MugVariantQueryService
import com.jotoai.voenix.shop.article.internal.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.assembler.MugWithVariantsSummaryAssembler
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.image.internal.service.ImageStorageServiceImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class MugVariantServiceImpl(
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: MugArticleVariantRepository,
    private val imageStorageService: ImageStorageServiceImpl,
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
    private val mugWithVariantsSummaryAssembler: MugWithVariantsSummaryAssembler,
) : MugVariantQueryService,
    MugVariantFacade {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Transactional
    override fun create(
        articleId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto {
        val article =
            articleRepository.findById(articleId).orElse(null)
                ?: throw ArticleNotFoundException("Article not found with id: $articleId")

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
                active = request.active,
            )

        val savedVariant = mugVariantRepository.save(variant)
        logger.debug { "Created mug variant ${savedVariant.id} for article $articleId with isDefault=$shouldBeDefault" }

        return mugArticleVariantAssembler.toDto(savedVariant)
    }

    @Transactional
    override fun update(
        variantId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ArticleNotFoundException("Mug variant not found with id: $variantId")

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
                logger.debug {
                    "Assigned default to variant ${newDefault.id} as variant $variantId is no longer default"
                }
            }
        }

        variant.apply {
            insideColorCode = request.insideColorCode
            outsideColorCode = request.outsideColorCode
            name = request.name
            articleVariantNumber = request.articleVariantNumber
            isDefault = request.isDefault
            active = request.active
        }

        val savedVariant = mugVariantRepository.save(variant)
        logger.debug { "Updated mug variant $variantId with isDefault=${request.isDefault}" }

        return mugArticleVariantAssembler.toDto(savedVariant)
    }

    @Transactional
    override fun delete(variantId: Long) {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ArticleNotFoundException("Mug variant not found with id: $variantId")

        val articleId = variant.article.id!!
        val wasDefault = variant.isDefault

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteMugVariantImage(filename)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to delete mug variant example image during variant deletion: $filename" }
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
                ?: throw ArticleNotFoundException("Mug variant not found with id: $variantId")

        // Delete the old image if it exists and is different from the new one
        variant.exampleImageFilename?.let { oldFilename ->
            if (oldFilename != filename) {
                try {
                    imageStorageService.deleteMugVariantImage(oldFilename)
                    logger.debug { "Deleted old mug variant image: $oldFilename" }
                } catch (e: IOException) {
                    logger.warn(e) { "Failed to delete old mug variant image: $oldFilename" }
                }
            }
        }

        variant.exampleImageFilename = filename
        return mugArticleVariantAssembler.toDto(mugVariantRepository.save(variant))
    }

    @Transactional
    override fun removeExampleImage(variantId: Long): MugArticleVariantDto {
        val variant =
            mugVariantRepository.findByIdWithArticle(variantId).orElse(null)
                ?: throw ArticleNotFoundException("Mug variant not found with id: $variantId")

        // Delete the image file if it exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteMugVariantImage(filename)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to delete mug variant example image: $filename" }
            }
        }

        // Clear the filename in the database
        variant.exampleImageFilename = null
        return mugArticleVariantAssembler.toDto(mugVariantRepository.save(variant))
    }

    @Transactional(readOnly = true)
    override fun findAllMugsWithVariants(excludeMugId: Long?): List<MugWithVariantsSummaryDto> {
        val articles = articleRepository.findAllMugsWithVariants(ArticleType.MUG, excludeMugId)
        return articles.map { mugWithVariantsSummaryAssembler.toDto(it) }
    }

    @Transactional
    override fun copyVariants(
        targetMugId: Long,
        request: CopyVariantsRequest,
    ): List<MugArticleVariantDto> {
        // Validate target mug exists
        val targetArticle =
            articleRepository.findById(targetMugId).orElse(null)
                ?: throw ArticleNotFoundException("Target mug not found with id: $targetMugId")

        if (targetArticle.articleType != ArticleType.MUG) {
            throw BadRequestException("Target article must be of type MUG")
        }

        // Validate all source variant IDs exist
        val sourceVariants = mugVariantRepository.findByIdInWithArticle(request.variantIds)
        if (sourceVariants.size != request.variantIds.size) {
            val foundIds = sourceVariants.map { it.id }
            val missingIds = request.variantIds - foundIds.toSet()
            throw BadRequestException("Variant(s) not found with id(s): $missingIds")
        }

        // Check if target mug has any existing variants to determine default behavior
        val existingVariants = mugVariantRepository.findByArticleId(targetMugId)
        val hasExistingDefault = existingVariants.any { it.isDefault }

        // Copy variants
        val copiedVariants =
            sourceVariants.map { sourceVariant ->
                val newVariant =
                    MugArticleVariant(
                        article = targetArticle,
                        insideColorCode = sourceVariant.insideColorCode,
                        outsideColorCode = sourceVariant.outsideColorCode,
                        name = sourceVariant.name,
                        articleVariantNumber = sourceVariant.articleVariantNumber,
                        // Never copy the isDefault flag - only set as default if there are no existing variants
                        isDefault =
                            !hasExistingDefault &&
                                existingVariants.isEmpty() &&
                                sourceVariant == sourceVariants.first(),
                        active = sourceVariant.active,
                        // Do not copy the image - only copy variant attributes
                        exampleImageFilename = null,
                    )
                mugVariantRepository.save(newVariant)
            }

        logger.info { "Copied ${copiedVariants.size} variants to mug $targetMugId" }

        return copiedVariants.map { mugArticleVariantAssembler.toDto(it) }
    }
}
