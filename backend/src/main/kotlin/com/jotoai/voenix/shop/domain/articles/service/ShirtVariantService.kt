package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.domain.articles.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.ShirtArticleVariantRepository
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShirtVariantService(
    private val articleRepository: ArticleRepository,
    private val shirtVariantRepository: ShirtArticleVariantRepository,
    private val imageStorageService: ImageStorageService,
    private val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ShirtVariantService::class.java)
    }

    @Transactional
    fun create(
        articleId: Long,
        request: CreateShirtArticleVariantRequest,
    ): ShirtArticleVariantDto {
        val article =
            articleRepository
                .findById(articleId)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

        val variant =
            ShirtArticleVariant(
                article = article,
                color = request.color,
                size = request.size,
                exampleImageFilename = request.exampleImageFilename,
            )

        return shirtArticleVariantAssembler.toDto(shirtVariantRepository.save(variant))
    }

    @Transactional
    fun update(
        variantId: Long,
        request: CreateShirtArticleVariantRequest,
    ): ShirtArticleVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Shirt variant not found with id: $variantId") }

        variant.apply {
            color = request.color
            size = request.size
            exampleImageFilename = request.exampleImageFilename
        }

        return shirtArticleVariantAssembler.toDto(shirtVariantRepository.save(variant))
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
                imageStorageService.deleteFile(filename, ImageType.SHIRT_VARIANT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete shirt variant example image during variant deletion: $filename", e)
            }
        }

        shirtVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): List<ShirtArticleVariantDto> =
        shirtVariantRepository.findByArticleId(articleId).map { shirtArticleVariantAssembler.toDto(it) }

    @Transactional
    fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): ShirtArticleVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Shirt variant not found with id: $variantId") }

        variant.exampleImageFilename = filename
        return shirtArticleVariantAssembler.toDto(shirtVariantRepository.save(variant))
    }
}
