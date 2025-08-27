package com.jotoai.voenix.shop.prompt.internal.repository

import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PromptSlotVariantRepository : JpaRepository<PromptSlotVariant, Long> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Long,
    ): Boolean

}
