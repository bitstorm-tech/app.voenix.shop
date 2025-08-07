package com.jotoai.voenix.shop.user.internal.entity

import com.jotoai.voenix.shop.user.api.dto.UserDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 255)
    var email: String,
    @Column(name = "first_name", length = 255)
    var firstName: String? = null,
    @Column(name = "last_name", length = 255)
    var lastName: String? = null,
    @Column(name = "phone_number", length = 255)
    var phoneNumber: String? = null,
    @get:JvmName("getPasswordValue")
    @Column(length = 255)
    var password: String? = null,
    @Column(name = "one_time_password", length = 255)
    var oneTimePassword: String? = null,
    @Column(name = "one_time_password_created_at")
    var oneTimePasswordCreatedAt: OffsetDateTime? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
    @Column(name = "deleted_at", columnDefinition = "timestamptz")
    var deletedAt: OffsetDateTime? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: Set<Role> = mutableSetOf(),
) {
    /**
     * Checks if the user is active (not soft deleted).
     */
    fun isActive(): Boolean = deletedAt == null

    /**
     * Marks the user as deleted with the current timestamp.
     */
    fun markAsDeleted() {
        deletedAt = OffsetDateTime.now()
    }

    /**
     * Restores a soft-deleted user.
     */
    fun restore() {
        deletedAt = null
    }

    fun toDto() =
        UserDto(
            id = requireNotNull(this.id) { "User ID cannot be null when converting to DTO" },
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            phoneNumber = this.phoneNumber,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return email == other.email
    }

    override fun hashCode(): Int = email.hashCode()
}
