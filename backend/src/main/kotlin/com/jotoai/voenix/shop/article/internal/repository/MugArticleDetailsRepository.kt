package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.article.internal.entity.MugArticleDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MugArticleDetailsRepository : JpaRepository<MugArticleDetails, Long> {
    fun findByArticleId(articleId: Long): MugArticleDetails?
}
