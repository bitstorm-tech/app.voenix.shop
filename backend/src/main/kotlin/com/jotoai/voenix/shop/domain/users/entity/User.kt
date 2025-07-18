package com.jotoai.voenix.shop.domain.users.entity

import com.jotoai.voenix.shop.auth.entity.Role
import com.jotoai.voenix.shop.domain.users.dto.UserDto
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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
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
) : UserDetails {
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

    override fun toString(): String =
        "User(id=$id, email='$email', firstName=$firstName, lastName=$lastName, " +
            "phoneNumber=$phoneNumber, password=${if (password != null) "[PROTECTED]" else null}, " +
            "oneTimePassword=${if (oneTimePassword != null) "[PROTECTED]" else null}, " +
            "createdAt=$createdAt, updatedAt=$updatedAt, roles=$roles)"

    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }

    override fun getUsername(): String = email

    override fun getPassword(): String? = this.password

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
