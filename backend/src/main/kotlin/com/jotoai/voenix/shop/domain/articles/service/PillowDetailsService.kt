package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.domain.articles.dto.ArticlePillowDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreatePillowDetailsRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdatePillowDetailsRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.ArticlePillowDetails
import com.jotoai.voenix.shop.domain.articles.repository.ArticlePillowDetailsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PillowDetailsService(
    private val pillowDetailsRepository: ArticlePillowDetailsRepository,
) {
    @Transactional(readOnly = true)
    fun findByArticleId(articleId: Long): ArticlePillowDetailsDto? = pillowDetailsRepository.findByArticleId(articleId)?.toDto()

    @Transactional
    fun create(
        article: Article,
        request: CreatePillowDetailsRequest,
    ): ArticlePillowDetailsDto {
        val details =
            ArticlePillowDetails(
                articleId = article.id!!,
                widthCm = request.widthCm,
                heightCm = request.heightCm,
                depthCm = request.depthCm,
                material = request.material,
                fillingType = request.fillingType,
                coverRemovable = request.coverRemovable,
                washable = request.washable,
            )

        return pillowDetailsRepository.save(details).toDto()
    }

    @Transactional
    fun update(
        article: Article,
        request: UpdatePillowDetailsRequest,
    ): ArticlePillowDetailsDto {
        val existingDetails = pillowDetailsRepository.findByArticleId(article.id!!)

        return if (existingDetails != null) {
            existingDetails.apply {
                widthCm = request.widthCm
                heightCm = request.heightCm
                depthCm = request.depthCm
                material = request.material
                fillingType = request.fillingType
                coverRemovable = request.coverRemovable
                washable = request.washable
            }
            pillowDetailsRepository.save(existingDetails).toDto()
        } else {
            create(
                article,
                CreatePillowDetailsRequest(
                    widthCm = request.widthCm,
                    heightCm = request.heightCm,
                    depthCm = request.depthCm,
                    material = request.material,
                    fillingType = request.fillingType,
                    coverRemovable = request.coverRemovable,
                    washable = request.washable,
                ),
            )
        }
    }
}
