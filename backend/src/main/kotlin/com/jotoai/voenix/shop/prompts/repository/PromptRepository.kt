package com.jotoai.voenix.shop.prompts.repository

import com.jotoai.voenix.shop.prompts.entity.Prompt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptRepository : JpaRepository<Prompt, Long> {
    
    fun findByTitleContainingIgnoreCase(title: String): List<Prompt>
    
    fun countByCategoryId(categoryId: Long): Int
}