package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShirtArticleVariantRepository : JpaRepository<ShirtArticleVariant, Long> {
    fun findByArticleId(articleId: Long): List<ShirtArticleVariant>

    fun deleteByArticleId(articleId: Long)
}
