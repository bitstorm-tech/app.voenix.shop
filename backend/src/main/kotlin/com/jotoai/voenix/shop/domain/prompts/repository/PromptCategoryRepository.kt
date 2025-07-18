package com.jotoai.voenix.shop.domain.prompts.repository

import com.jotoai.voenix.shop.domain.prompts.entity.PromptCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptCategoryRepository : JpaRepository<PromptCategory, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<PromptCategory>
}
