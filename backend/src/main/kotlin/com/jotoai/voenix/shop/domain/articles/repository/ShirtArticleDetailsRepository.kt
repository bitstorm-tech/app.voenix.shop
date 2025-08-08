package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShirtArticleDetailsRepository : JpaRepository<ShirtArticleDetails, Long> {
    fun findByArticleId(articleId: Long): ShirtArticleDetails?
}
