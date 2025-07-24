package com.jotoai.voenix.shop.domain.vat.entity

import com.jotoai.voenix.shop.domain.vat.dto.ValueAddedTaxDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "value_added_taxes")
data class ValueAddedTax(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @Column(nullable = false)
    var percent: Int,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ValueAddedTaxDto(
            id = requireNotNull(this.id) { "ValueAddedTax ID cannot be null when converting to DTO" },
            name = this.name,
            percent = this.percent,
            description = this.description,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
