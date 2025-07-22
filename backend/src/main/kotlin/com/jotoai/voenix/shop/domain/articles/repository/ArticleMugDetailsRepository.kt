package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleMugDetailsRepository : JpaRepository<ArticleMugDetails, Long> {
    fun findByArticleId(articleId: Long): ArticleMugDetails?
}
