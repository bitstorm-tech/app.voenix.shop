package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.entity.Article
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : JpaRepository<Article, Long> {
    @Query(
        """
        SELECT a FROM Article a 
        LEFT JOIN FETCH a.category 
        LEFT JOIN FETCH a.subcategory 
        WHERE (:articleType IS NULL OR a.articleType = :articleType)
        AND (:categoryId IS NULL OR a.category.id = :categoryId)
        AND (:subcategoryId IS NULL OR a.subcategory.id = :subcategoryId)
        AND (:active IS NULL OR a.active = :active)
    """,
    )
    fun findAllWithFilters(
        @Param("articleType") articleType: ArticleType?,
        @Param("categoryId") categoryId: Long?,
        @Param("subcategoryId") subcategoryId: Long?,
        @Param("active") active: Boolean?,
        pageable: Pageable,
    ): Page<Article>

    @Query(
        """
        SELECT a FROM Article a 
        LEFT JOIN FETCH a.category 
        LEFT JOIN FETCH a.subcategory 
        WHERE a.id = :id
    """,
    )
    fun findByIdWithBasicDetails(
        @Param("id") id: Long,
    ): Article?

    @Query(
        """
        SELECT a FROM Article a 
        LEFT JOIN FETCH a.category 
        LEFT JOIN FETCH a.subcategory 
        LEFT JOIN FETCH a.mugVariants 
        WHERE a.id = :id
    """,
    )
    fun findMugByIdWithDetails(
        @Param("id") id: Long,
    ): Article?

    @Query(
        """
        SELECT a FROM Article a 
        LEFT JOIN FETCH a.category 
        LEFT JOIN FETCH a.subcategory 
        LEFT JOIN FETCH a.shirtVariants 
        WHERE a.id = :id
    """,
    )
    fun findShirtByIdWithDetails(
        @Param("id") id: Long,
    ): Article?

    fun findByArticleType(articleType: ArticleType): List<Article>

    fun findByCategoryId(categoryId: Long): List<Article>

    fun findBySubcategoryId(subcategoryId: Long): List<Article>

    @Query(
        """
        SELECT DISTINCT a FROM Article a 
        LEFT JOIN FETCH a.mugVariants 
        LEFT JOIN FETCH a.costCalculation 
        WHERE a.articleType = :articleType 
        AND a.active = true 
        ORDER BY a.id DESC
    """,
    )
    fun findAllActiveMugsWithDetails(
        @Param("articleType") articleType: ArticleType,
    ): List<Article>
}
