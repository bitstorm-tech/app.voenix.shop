package com.jotoai.voenix.shop.article.internal.categories.repository

import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleSubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleSubCategoryRepository : JpaRepository<ArticleSubCategory, Long> {
    fun findByArticleCategoryId(articleCategoryId: Long): List<ArticleSubCategory>

    fun existsByArticleCategoryIdAndNameIgnoreCase(
        articleCategoryId: Long,
        name: String,
    ): Boolean
}
