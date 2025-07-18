package com.jotoai.voenix.shop.domain.mugs.repository

import com.jotoai.voenix.shop.domain.mugs.entity.MugSubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MugSubCategoryRepository : JpaRepository<MugSubCategory, Long> {
    fun findByMugCategoryId(mugCategoryId: Long): List<MugSubCategory>

    fun findByNameContainingIgnoreCase(name: String): List<MugSubCategory>

    fun findByMugCategoryIdAndNameContainingIgnoreCase(
        mugCategoryId: Long,
        name: String,
    ): List<MugSubCategory>

    fun existsByMugCategoryIdAndNameIgnoreCase(
        mugCategoryId: Long,
        name: String,
    ): Boolean
}
