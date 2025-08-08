package com.jotoai.voenix.shop.prompt.internal.repository

import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptSubCategoryRepository : JpaRepository<PromptSubCategory, Long> {
    fun findByPromptCategoryId(promptCategoryId: Long): List<PromptSubCategory>

    fun findByNameContainingIgnoreCase(name: String): List<PromptSubCategory>

    fun countByPromptCategoryId(promptCategoryId: Long): Long

    fun existsByPromptCategoryIdAndName(
        promptCategoryId: Long,
        name: String,
    ): Boolean
}
