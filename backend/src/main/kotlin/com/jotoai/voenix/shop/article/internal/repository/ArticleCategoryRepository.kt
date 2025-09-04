package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.article.internal.entity.ArticleCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleCategoryRepository : JpaRepository<ArticleCategory, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
