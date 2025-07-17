package com.jotoai.voenix.shop.auth.entity

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
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 50)
    var name: String,
    @Column(length = 255)
    var description: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    override fun toString(): String = "Role(id=$id, name='$name', description=$description, createdAt=$createdAt, updatedAt=$updatedAt)"
}
