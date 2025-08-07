package com.jotoai.voenix.shop.supplier.internal.entity

import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
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
@Table(name = "suppliers")
class Supplier(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "name", length = 255)
    var name: String? = null,
    @Column(name = "title", length = 100)
    var title: String? = null,
    @Column(name = "first_name", length = 255)
    var firstName: String? = null,
    @Column(name = "last_name", length = 255)
    var lastName: String? = null,
    @Column(name = "street", length = 255)
    var street: String? = null,
    @Column(name = "house_number", length = 50)
    var houseNumber: String? = null,
    @Column(name = "city", length = 255)
    var city: String? = null,
    @Column(name = "postal_code")
    var postalCode: Int? = null,
    @Column(name = "country_id")
    var countryId: Long? = null,
    @Column(name = "phone_number1", length = 50)
    var phoneNumber1: String? = null,
    @Column(name = "phone_number2", length = 50)
    var phoneNumber2: String? = null,
    @Column(name = "phone_number3", length = 50)
    var phoneNumber3: String? = null,
    @Column(name = "email", length = 255)
    var email: String? = null,
    @Column(name = "website", length = 500)
    var website: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        SupplierDto(
            id = requireNotNull(this.id) { "Supplier ID cannot be null when converting to DTO" },
            name = this.name,
            title = this.title,
            firstName = this.firstName,
            lastName = this.lastName,
            street = this.street,
            houseNumber = this.houseNumber,
            city = this.city,
            postalCode = this.postalCode,
            countryId = this.countryId,
            countryName = null, // Will be populated by service layer
            phoneNumber1 = this.phoneNumber1,
            phoneNumber2 = this.phoneNumber2,
            phoneNumber3 = this.phoneNumber3,
            email = this.email,
            website = this.website,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
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
