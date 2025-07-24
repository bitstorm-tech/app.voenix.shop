package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleMugVariantRepository : JpaRepository<ArticleMugVariant, Long> {
    fun findByArticleId(articleId: Long): List<ArticleMugVariant>

    fun deleteByArticleId(articleId: Long)

    fun existsBySku(sku: String): Boolean

    fun existsBySkuAndIdNot(
        sku: String,
        id: Long,
    ): Boolean
}
