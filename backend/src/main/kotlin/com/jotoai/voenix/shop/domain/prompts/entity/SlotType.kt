package com.jotoai.voenix.shop.domain.prompts.entity

import com.jotoai.voenix.shop.domain.prompts.dto.SlotTypeDto
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
@Table(name = "slot_types")
data class SlotType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @Column(nullable = false, unique = true)
    var position: Int,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        SlotTypeDto(
            id = requireNotNull(this.id) { "SlotType ID cannot be null when converting to DTO" },
            name = this.name,
            position = this.position,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
