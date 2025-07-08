package com.jotoai.voenix.shop.prompts.repository

import com.jotoai.voenix.shop.prompts.entity.SlotType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SlotTypeRepository : JpaRepository<SlotType, Long> {
    
    fun existsByName(name: String): Boolean
    
    fun existsByNameAndIdNot(name: String, id: Long): Boolean
}