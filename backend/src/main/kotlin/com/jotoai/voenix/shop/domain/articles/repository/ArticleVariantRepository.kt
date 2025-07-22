package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleVariantRepository : JpaRepository<ArticleVariant, Long> {
    fun findByArticleId(articleId: Long): List<ArticleVariant>

    fun existsBySku(sku: String): Boolean

    fun deleteByArticleId(articleId: Long)
}
