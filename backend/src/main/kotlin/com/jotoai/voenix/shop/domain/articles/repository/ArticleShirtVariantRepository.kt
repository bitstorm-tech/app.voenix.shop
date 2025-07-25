package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleShirtVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleShirtVariantRepository : JpaRepository<ArticleShirtVariant, Long> {
    fun findByArticleId(articleId: Long): List<ArticleShirtVariant>

    fun deleteByArticleId(articleId: Long)
}
