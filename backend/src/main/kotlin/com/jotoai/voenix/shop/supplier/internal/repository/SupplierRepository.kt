package com.jotoai.voenix.shop.supplier.internal.repository

import com.jotoai.voenix.shop.supplier.internal.entity.Supplier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplierRepository : JpaRepository<Supplier, Long> {
    fun existsByName(name: String?): Boolean

    fun existsByNameAndIdNot(
        name: String?,
        id: Long,
    ): Boolean

    fun existsByEmail(email: String?): Boolean

    fun existsByEmailAndIdNot(
        email: String?,
        id: Long,
    ): Boolean
}
