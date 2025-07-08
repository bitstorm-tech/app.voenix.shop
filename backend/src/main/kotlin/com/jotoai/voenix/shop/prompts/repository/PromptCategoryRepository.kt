package com.jotoai.voenix.shop.prompts.repository

import com.jotoai.voenix.shop.prompts.entity.PromptCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptCategoryRepository : JpaRepository<PromptCategory, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<PromptCategory>
}
