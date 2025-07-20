package com.jotoai.voenix.shop.domain.articles.categories.repository

import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleCategoryRepository : JpaRepository<ArticleCategory, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<ArticleCategory>

    fun existsByNameIgnoreCase(name: String): Boolean
}
