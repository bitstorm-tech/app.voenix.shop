package com.jotoai.voenix.shop.article.internal.categories.repository

import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleCategoryRepository : JpaRepository<ArticleCategory, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
