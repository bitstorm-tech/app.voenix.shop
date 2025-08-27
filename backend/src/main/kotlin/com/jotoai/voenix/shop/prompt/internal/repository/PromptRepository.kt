package com.jotoai.voenix.shop.prompt.internal.repository

import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PromptRepository : JpaRepository<Prompt, Long> {
    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.subcategory WHERE p.id = :id")
    fun findByIdWithRelations(
        @Param("id") id: Long,
    ): Optional<Prompt>

    @Query("SELECT p FROM Prompt p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.subcategory ORDER BY p.id DESC")
    fun findAllWithRelations(): List<Prompt>

    @Query(
        "SELECT DISTINCT p FROM Prompt p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.subcategory " +
            "LEFT JOIN FETCH p.promptSlotVariantMappings m " +
            "LEFT JOIN FETCH m.promptSlotVariant v " +
            "LEFT JOIN FETCH v.promptSlotType " +
            "WHERE p.active = true " +
            "ORDER BY p.id DESC",
    )
    fun findAllActiveWithRelations(): List<Prompt>

    fun countByCategoryId(categoryId: Long): Int

    fun countBySubcategoryId(subcategoryId: Long): Int
}
