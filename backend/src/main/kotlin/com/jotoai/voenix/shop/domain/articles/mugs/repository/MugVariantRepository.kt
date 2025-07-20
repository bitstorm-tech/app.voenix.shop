package com.jotoai.voenix.shop.domain.articles.mugs.repository

import com.jotoai.voenix.shop.domain.articles.mugs.entity.MugVariant
import org.springframework.data.jpa.repository.JpaRepository

interface MugVariantRepository : JpaRepository<MugVariant, Long> {
    fun findByMugId(mugId: Long): List<MugVariant>

    fun findByMugIdOrderByColorCode(mugId: Long): List<MugVariant>

    fun existsByMugIdAndColorCode(
        mugId: Long,
        colorCode: String,
    ): Boolean
}
