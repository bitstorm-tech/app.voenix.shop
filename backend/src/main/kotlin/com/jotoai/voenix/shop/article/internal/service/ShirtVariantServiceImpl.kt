package com.jotoai.voenix.shop.article.internal.service
import com.jotoai.voenix.shop.article.internal.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.article.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.article.internal.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.ShirtArticleVariantRepository
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class ShirtVariantServiceImpl(
    private val articleRepository: ArticleRepository,
    private val shirtVariantRepository: ShirtArticleVariantRepository,
    private val imageService: ImageService,
    private val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Transactional
    fun create(
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
    fun update(
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
    fun delete(variantId: Long) {
        val variant =
            shirtVariantRepository
                .findById(variantId)
                .orElseThrow { ArticleNotFoundException("Shirt variant not found with id: $variantId") }

        // Delete associated image if exists
        variant.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.SHIRT_VARIANT_EXAMPLE)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to delete shirt variant example image during variant deletion: $filename" }
            }
        }

        shirtVariantRepository.deleteById(variantId)
    }
}
