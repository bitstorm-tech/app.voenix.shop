package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.domain.articles.dto.CreateShirtDetailsRequest
import com.jotoai.voenix.shop.domain.articles.dto.ShirtArticleDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.UpdateShirtDetailsRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleDetails
import com.jotoai.voenix.shop.domain.articles.repository.ShirtArticleDetailsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ShirtDetailsService(
    private val shirtDetailsRepository: ShirtArticleDetailsRepository,
) {
    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): ShirtArticleDetailsDto? = shirtDetailsRepository.findByArticleId(articleId)?.toDto()

    @Transactional
    fun create(
        article: Article,
        request: CreateShirtDetailsRequest,
    ): ShirtArticleDetailsDto {
        val details =
            ShirtArticleDetails(
                articleId = article.id!!,
                material = request.material,
                careInstructions = request.careInstructions,
                fitType = request.fitType,
                availableSizes = request.availableSizes.toTypedArray(),
            )

        return shirtDetailsRepository.save(details).toDto()
    }

    @Transactional
    fun update(
        article: Article,
        request: UpdateShirtDetailsRequest,
    ): ShirtArticleDetailsDto {
        val existingDetails = shirtDetailsRepository.findByArticleId(article.id!!)

        return if (existingDetails != null) {
            existingDetails.apply {
                material = request.material
                careInstructions = request.careInstructions
                fitType = request.fitType
                availableSizes = request.availableSizes.toTypedArray()
            }
            shirtDetailsRepository.save(existingDetails).toDto()
        } else {
            create(
                article,
                CreateShirtDetailsRequest(
                    material = request.material,
                    careInstructions = request.careInstructions,
                    fitType = request.fitType,
                    availableSizes = request.availableSizes,
                ),
            )
        }
    }
}
