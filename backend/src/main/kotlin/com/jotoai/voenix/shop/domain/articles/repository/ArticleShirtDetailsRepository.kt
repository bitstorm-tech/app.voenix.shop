package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleShirtDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleShirtDetailsRepository : JpaRepository<ArticleShirtDetails, Long> {
    fun findByArticleId(articleId: Long): ArticleShirtDetails?
}
