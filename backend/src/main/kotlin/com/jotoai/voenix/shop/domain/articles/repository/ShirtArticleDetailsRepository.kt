package com.jotoai.voenix.shop.domain.articles.repository

import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShirtArticleDetailsRepository : JpaRepository<ShirtArticleDetails, Long> {
    fun findByArticleId(articleId: Long): ShirtArticleDetails?
}
