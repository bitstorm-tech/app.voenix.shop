package com.jotoai.voenix.shop.article.internal.repository

import com.jotoai.voenix.shop.domain.articles.entity.CostCalculation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CostCalculationRepository : JpaRepository<CostCalculation, Long> {
    fun findByArticleId(articleId: Long): Optional<CostCalculation>
}
