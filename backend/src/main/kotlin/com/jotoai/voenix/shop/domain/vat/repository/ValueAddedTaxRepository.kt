package com.jotoai.voenix.shop.domain.vat.repository

import com.jotoai.voenix.shop.domain.vat.entity.ValueAddedTax
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ValueAddedTaxRepository : JpaRepository<ValueAddedTax, Long> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: Long,
    ): Boolean
}
