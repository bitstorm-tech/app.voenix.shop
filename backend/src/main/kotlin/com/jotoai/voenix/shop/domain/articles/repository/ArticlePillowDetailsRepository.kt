package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticlePillowDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticlePillowDetailsRepository : JpaRepository<ArticlePillowDetails, Long> {
    fun findByArticleId(articleId: Long): ArticlePillowDetails?
}
