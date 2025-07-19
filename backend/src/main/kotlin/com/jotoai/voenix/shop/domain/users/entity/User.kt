package com.jotoai.voenix.shop.domain.users.entity

import com.jotoai.voenix.shop.auth.entity.Role
import com.jotoai.voenix.shop.domain.users.dto.UserDto
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
data class User(
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
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")],
    )
    var roles: Set<Role> = mutableSetOf(),
) {
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
}
