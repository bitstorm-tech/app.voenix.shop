package com.jotoai.voenix.shop.domain.articles.categories.repository

import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleSubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleSubCategoryRepository : JpaRepository<ArticleSubCategory, Long> {
    fun findByArticleCategoryId(articleCategoryId: Long): List<ArticleSubCategory>

    fun findByNameContainingIgnoreCase(name: String): List<ArticleSubCategory>

    fun findByArticleCategoryIdAndNameContainingIgnoreCase(
        articleCategoryId: Long,
        name: String,
    ): List<ArticleSubCategory>

    fun existsByArticleCategoryIdAndNameIgnoreCase(
        articleCategoryId: Long,
        name: String,
    ): Boolean
}
