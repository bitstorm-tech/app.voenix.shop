package com.jotoai.voenix.shop.domain.mugs.repository

import com.jotoai.voenix.shop.domain.mugs.entity.Mug
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MugRepository : JpaRepository<Mug, Long> {
    fun findByActiveTrue(): List<Mug>

    fun findByNameContainingIgnoreCaseAndActiveTrue(name: String): List<Mug>

    fun findByPriceBetweenAndActiveTrue(
        minPrice: Int,
        maxPrice: Int,
    ): List<Mug>
}
