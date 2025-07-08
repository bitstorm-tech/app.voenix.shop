package com.jotoai.voenix.shop.prompts.repository

import com.jotoai.voenix.shop.prompts.entity.Slot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SlotRepository : JpaRepository<Slot, Long> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Long,
    ): Boolean

    fun findBySlotTypeId(slotTypeId: Long): List<Slot>
}
