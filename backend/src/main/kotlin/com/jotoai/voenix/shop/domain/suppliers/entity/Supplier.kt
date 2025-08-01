package com.jotoai.voenix.shop.domain.suppliers.entity

import com.jotoai.voenix.shop.domain.countries.entity.Country
import com.jotoai.voenix.shop.domain.suppliers.dto.SupplierDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "suppliers")
class Supplier(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name")
    val name: String? = null,
    @Column(name = "title")
    val title: String? = null,
    @Column(name = "first_name")
    val firstName: String? = null,
    @Column(name = "last_name")
    val lastName: String? = null,
    @Column(name = "street")
    val street: String? = null,
    @Column(name = "house_number")
    val houseNumber: String? = null,
    @Column(name = "city")
    val city: String? = null,
    @Column(name = "postal_code")
    val postalCode: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    val country: Country? = null,
    @Column(name = "phone_number1")
    val phoneNumber1: String? = null,
    @Column(name = "phone_number2")
    val phoneNumber2: String? = null,
    @Column(name = "phone_number3")
    val phoneNumber3: String? = null,
    @Column(name = "email")
    val email: String? = null,
    @Column(name = "website")
    val website: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDto(): SupplierDto =
        SupplierDto(
            id = id!!,
            name = name,
            title = title,
            firstName = firstName,
            lastName = lastName,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            country = country?.toDto(),
            phoneNumber1 = phoneNumber1,
            phoneNumber2 = phoneNumber2,
            phoneNumber3 = phoneNumber3,
            email = email,
            website = website,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Supplier) return false

        // If both have names, compare them
        if (name != null && other.name != null) {
            return name == other.name
        }

        // If names are null, use id-based comparison
        return id != null && id == other.id
    }

    override fun hashCode(): Int = name?.hashCode() ?: (id?.hashCode() ?: javaClass.hashCode())
}
