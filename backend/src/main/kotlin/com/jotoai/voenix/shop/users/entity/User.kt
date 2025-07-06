package com.jotoai.voenix.shop.users.entity

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
    
    @Column(length = 255)
    var password: String? = null,
    
    @Column(name = "one_time_password", length = 255)
    var oneTimePassword: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null
) {
    override fun toString(): String {
        return "User(id=$id, email='$email', firstName=$firstName, lastName=$lastName, " +
               "phoneNumber=$phoneNumber, password=${if (password != null) "[PROTECTED]" else null}, " +
               "oneTimePassword=${if (oneTimePassword != null) "[PROTECTED]" else null}, " +
               "createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}