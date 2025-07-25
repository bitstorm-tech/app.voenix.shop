package com.jotoai.voenix.shop.domain.prompts.repository

import com.jotoai.voenix.shop.domain.prompts.entity.PromptSlotType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptSlotTypeRepository : JpaRepository<PromptSlotType, Long> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Long,
    ): Boolean

    fun existsByPosition(position: Int): Boolean

    fun existsByPositionAndIdNot(
        position: Int,
        id: Long,
    ): Boolean
}
