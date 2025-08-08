package com.jotoai.voenix.shop.prompt.internal.repository

import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptCategoryRepository : JpaRepository<PromptCategory, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<PromptCategory>
}
