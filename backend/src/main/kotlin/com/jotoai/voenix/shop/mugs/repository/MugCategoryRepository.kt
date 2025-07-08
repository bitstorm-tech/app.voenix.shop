package com.jotoai.voenix.shop.mugs.repository

import com.jotoai.voenix.shop.mugs.entity.MugCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MugCategoryRepository : JpaRepository<MugCategory, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<MugCategory>

    fun existsByNameIgnoreCase(name: String): Boolean
}
