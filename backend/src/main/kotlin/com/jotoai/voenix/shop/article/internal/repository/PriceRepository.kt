package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.article.internal.entity.Price
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PriceRepository : JpaRepository<Price, Long> {
    fun findByArticleId(articleId: Long): Optional<Price>
}
