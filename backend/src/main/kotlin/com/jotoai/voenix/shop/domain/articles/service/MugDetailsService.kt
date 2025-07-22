package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.domain.articles.dto.ArticleMugDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugDetailsRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateMugDetailsRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugDetails
import com.jotoai.voenix.shop.domain.articles.repository.ArticleMugDetailsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MugDetailsService(
    private val mugDetailsRepository: ArticleMugDetailsRepository,
) {
    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): ArticleMugDetailsDto? = mugDetailsRepository.findByArticleId(articleId)?.toDto()

    @Transactional
    fun create(
        article: Article,
        request: CreateMugDetailsRequest,
    ): ArticleMugDetailsDto {
        val details =
            ArticleMugDetails(
                articleId = article.id!!,
                heightMm = request.heightMm,
                diameterMm = request.diameterMm,
                printTemplateWidthMm = request.printTemplateWidthMm,
                printTemplateHeightMm = request.printTemplateHeightMm,
                fillingQuantity = request.fillingQuantity,
                dishwasherSafe = request.dishwasherSafe,
            )

        return mugDetailsRepository.saveAndFlush(details).toDto()
    }

    @Transactional
    fun update(
        article: Article,
        request: UpdateMugDetailsRequest,
    ): ArticleMugDetailsDto {
        val existingDetails = mugDetailsRepository.findByArticleId(article.id!!)

        return if (existingDetails != null) {
            existingDetails.apply {
                heightMm = request.heightMm
                diameterMm = request.diameterMm
                printTemplateWidthMm = request.printTemplateWidthMm
                printTemplateHeightMm = request.printTemplateHeightMm
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
                    fillingQuantity = request.fillingQuantity,
                    dishwasherSafe = request.dishwasherSafe,
                ),
            )
        }
    }
}
