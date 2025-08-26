package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.CreateMugDetailsRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto
import com.jotoai.voenix.shop.article.api.dto.UpdateMugDetailsRequest
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.MugArticleDetails
import com.jotoai.voenix.shop.article.internal.repository.MugArticleDetailsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugDetailsService(
    private val mugDetailsRepository: MugArticleDetailsRepository,
) {
    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): MugArticleDetailsDto? =
        mugDetailsRepository.findByArticleId(articleId)?.toDto()

    @Transactional
    fun create(
        article: Article,
        request: CreateMugDetailsRequest,
    ): MugArticleDetailsDto {
        validateDocumentFormatDimensions(
            request.documentFormatWidthMm,
            request.documentFormatHeightMm,
            request.printTemplateWidthMm,
            request.printTemplateHeightMm,
        )

        val details =
            MugArticleDetails(
                articleId = article.id!!,
                heightMm = request.heightMm,
                diameterMm = request.diameterMm,
                printTemplateWidthMm = request.printTemplateWidthMm,
                printTemplateHeightMm = request.printTemplateHeightMm,
                documentFormatWidthMm = request.documentFormatWidthMm,
                documentFormatHeightMm = request.documentFormatHeightMm,
                documentFormatMarginBottomMm = request.documentFormatMarginBottomMm,
                fillingQuantity = request.fillingQuantity,
                dishwasherSafe = request.dishwasherSafe,
            )

        return mugDetailsRepository.saveAndFlush(details).toDto()
    }

    @Transactional
    fun update(
        article: Article,
        request: UpdateMugDetailsRequest,
    ): MugArticleDetailsDto {
        validateDocumentFormatDimensions(
            request.documentFormatWidthMm,
            request.documentFormatHeightMm,
            request.printTemplateWidthMm,
            request.printTemplateHeightMm,
        )

        val existingDetails = mugDetailsRepository.findByArticleId(article.id!!)

        return if (existingDetails != null) {
            existingDetails.apply {
                heightMm = request.heightMm
                diameterMm = request.diameterMm
                printTemplateWidthMm = request.printTemplateWidthMm
                printTemplateHeightMm = request.printTemplateHeightMm
                documentFormatWidthMm = request.documentFormatWidthMm
                documentFormatHeightMm = request.documentFormatHeightMm
                documentFormatMarginBottomMm = request.documentFormatMarginBottomMm
                fillingQuantity = request.fillingQuantity
                dishwasherSafe = request.dishwasherSafe
            }
            mugDetailsRepository.saveAndFlush(existingDetails).toDto()
        } else {
            create(
                article,
                CreateMugDetailsRequest(
                    heightMm = request.heightMm,
                    diameterMm = request.diameterMm,
                    printTemplateWidthMm = request.printTemplateWidthMm,
                    printTemplateHeightMm = request.printTemplateHeightMm,
                    documentFormatWidthMm = request.documentFormatWidthMm,
                    documentFormatHeightMm = request.documentFormatHeightMm,
                    documentFormatMarginBottomMm = request.documentFormatMarginBottomMm,
                    fillingQuantity = request.fillingQuantity,
                    dishwasherSafe = request.dishwasherSafe,
                ),
            )
        }
    }

    private fun validateDocumentFormatDimensions(
        documentFormatWidthMm: Int?,
        documentFormatHeightMm: Int?,
        printTemplateWidthMm: Int,
        printTemplateHeightMm: Int,
    ) {
        documentFormatWidthMm?.let { docWidth ->
            require(docWidth > printTemplateWidthMm) {
                "Document format width ($docWidth mm) must be greater than " +
                    "print template width ($printTemplateWidthMm mm)"
            }
        }

        documentFormatHeightMm?.let { docHeight ->
            require(docHeight > printTemplateHeightMm) {
                "Document format height ($docHeight mm) must be greater than " +
                    "print template height ($printTemplateHeightMm mm)"
            }
        }
    }
}
