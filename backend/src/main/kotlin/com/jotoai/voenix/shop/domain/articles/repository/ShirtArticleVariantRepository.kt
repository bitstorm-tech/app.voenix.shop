package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShirtArticleVariantRepository : JpaRepository<ShirtArticleVariant, Long> {
    fun findByArticleId(articleId: Long): List<ShirtArticleVariant>

    fun deleteByArticleId(articleId: Long)
}
