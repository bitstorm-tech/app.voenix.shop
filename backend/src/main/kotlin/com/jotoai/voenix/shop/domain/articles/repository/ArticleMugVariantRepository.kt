package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ArticleMugVariantRepository : JpaRepository<ArticleMugVariant, Long> {
    @Query("SELECT v FROM ArticleMugVariant v JOIN FETCH v.article WHERE v.article.id = :articleId")
    fun findByArticleIdWithArticle(
        @Param("articleId") articleId: Long,
    ): List<ArticleMugVariant>

    @Query("SELECT v FROM ArticleMugVariant v JOIN FETCH v.article WHERE v.id = :id")
    fun findByIdWithArticle(
        @Param("id") id: Long,
    ): Optional<ArticleMugVariant>

    @Query("SELECT v FROM ArticleMugVariant v WHERE v.article.id = :articleId")
    fun findByArticleId(
        @Param("articleId") articleId: Long,
    ): List<ArticleMugVariant>

    fun deleteByArticleId(articleId: Long)

    fun existsBySku(sku: String): Boolean

    fun existsBySkuAndIdNot(
        sku: String,
        id: Long,
    ): Boolean
}
