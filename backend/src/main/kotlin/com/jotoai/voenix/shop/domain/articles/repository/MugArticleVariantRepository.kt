package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MugArticleVariantRepository : JpaRepository<MugArticleVariant, Long> {
    @Query("SELECT v FROM MugArticleVariant v JOIN FETCH v.article WHERE v.article.id = :articleId")
    fun findByArticleIdWithArticle(
        @Param("articleId") articleId: Long,
    ): List<MugArticleVariant>

    @Query("SELECT v FROM MugArticleVariant v JOIN FETCH v.article WHERE v.id = :id")
    fun findByIdWithArticle(
        @Param("id") id: Long,
    ): Optional<MugArticleVariant>

    @Query("SELECT v FROM MugArticleVariant v WHERE v.article.id = :articleId")
    fun findByArticleId(
        @Param("articleId") articleId: Long,
    ): List<MugArticleVariant>

    fun deleteByArticleId(articleId: Long)

    @Query("UPDATE MugArticleVariant v SET v.isDefault = false WHERE v.article.id = :articleId AND v.isDefault = true")
    @Modifying
    fun unsetDefaultForArticle(
        @Param("articleId") articleId: Long,
    )
}
