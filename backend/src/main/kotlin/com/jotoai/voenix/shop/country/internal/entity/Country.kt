package com.jotoai.voenix.shop.country.internal.entity

import com.jotoai.voenix.shop.country.api.dto.CountryDto
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
@Table(name = "countries")
class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        CountryDto(
            id = requireNotNull(this.id) { "Country ID cannot be null when converting to DTO" },
            name = this.name,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Country) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}
