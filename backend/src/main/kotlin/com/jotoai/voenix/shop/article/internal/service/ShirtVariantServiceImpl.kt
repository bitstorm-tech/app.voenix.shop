package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantFacade
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantQueryService
import com.jotoai.voenix.shop.article.internal.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.ShirtArticleVariantRepository
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class ShirtVariantServiceImpl(
    private val articleRepository: ArticleRepository,
    private val shirtVariantRepository: ShirtArticleVariantRepository,
    private val imageStorageService: ImageStorageService,
    private val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
) : ShirtVariantQueryService,
    ShirtVariantFacade {
    companion object {
        private val logger = LoggerFactory.getLogger(ShirtVariantServiceImpl::class.java)
    }

    @Transactional
    override fun create(
        articleId: Long,
        request: CreateShirtArticleVariantRequest,
    ): ShirtArticleVariantDto {
        val article =
            articleRepository
                .findById(articleId)
                .orElseThrow { ArticleNotFoundException("Article not found with id: $articleId") }

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
    override fun update(
        variantId: Long,
        request: CreateShirtArticleVariantRequest,
    ): ShirtArticleVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ArticleNotFoundException("Shirt variant not found with id: $variantId") }

        variant.apply {
            color = request.color
            size = request.size
            exampleImageFilename = request.exampleImageFilename
        }

        return shirtArticleVariantAssembler.toDto(shirtVariantRepository.save(variant))
    }

    @Transactional
    override fun delete(variantId: Long) {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ArticleNotFoundException("Shirt variant not found with id: $variantId") }

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteFile(filename, ImageType.SHIRT_VARIANT_EXAMPLE)
            } catch (e: IOException) {
                logger.warn("Failed to delete shirt variant example image during variant deletion: $filename", e)
            }
        }

        shirtVariantRepository.deleteById(variantId)
    }

    @Transactional(readOnly = true)
    override fun findByArticleId(articleId: Long): List<ShirtArticleVariantDto> =
        shirtVariantRepository.findByArticleId(articleId).map { shirtArticleVariantAssembler.toDto(it) }

    @Transactional
    override fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): ShirtArticleVariantDto {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ArticleNotFoundException("Shirt variant not found with id: $variantId") }

        variant.exampleImageFilename = filename
        return shirtArticleVariantAssembler.toDto(shirtVariantRepository.save(variant))
    }
}
