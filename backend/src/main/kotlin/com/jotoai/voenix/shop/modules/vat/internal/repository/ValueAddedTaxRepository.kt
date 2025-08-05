package com.jotoai.voenix.shop.modules.vat.internal.repository

import com.jotoai.voenix.shop.modules.vat.internal.entity.ValueAddedTax
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ValueAddedTaxRepository : JpaRepository<ValueAddedTax, Long> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Long,
    ): Boolean

    fun findByIsDefaultTrue(): Optional<ValueAddedTax>

    @Modifying
    @Query("UPDATE ValueAddedTax v SET v.isDefault = false WHERE v.isDefault = true AND v.id != :id")
    fun clearDefaultExcept(
        @Param("id") id: Long,
    )
}
