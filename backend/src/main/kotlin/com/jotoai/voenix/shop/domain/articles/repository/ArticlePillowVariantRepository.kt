package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ArticlePillowVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticlePillowVariantRepository : JpaRepository<ArticlePillowVariant, Long> {
    fun findByArticleId(articleId: Long): List<ArticlePillowVariant>

    fun deleteByArticleId(articleId: Long)
}
