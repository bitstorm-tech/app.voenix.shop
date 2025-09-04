package com.jotoai.voenix.shop.vat.internal.entity

import com.jotoai.voenix.shop.vat.internal.dto.ValueAddedTaxDto
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
@Suppress("LongParameterList")
class ValueAddedTax(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @Column(nullable = false)
    var percent: Int,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,
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
            isDefault = this.isDefault,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueAddedTax) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}
