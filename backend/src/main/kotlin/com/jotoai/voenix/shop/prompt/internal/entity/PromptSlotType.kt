package com.jotoai.voenix.shop.prompt.internal.entity

import com.jotoai.voenix.shop.prompt.PromptSlotTypeDto
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptSlotTypeDto
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
@Table(name = "prompt_slot_types")
class PromptSlotType(
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
        PromptSlotTypeDto(
            id = requireNotNull(this.id) { "PromptSlotType ID cannot be null when converting to DTO" },
            name = this.name,
            position = this.position,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun toPublicDto() =
        PublicPromptSlotTypeDto(
            id = requireNotNull(this.id) { "PromptSlotType ID cannot be null when converting to DTO" },
            name = this.name,
            position = this.position,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptSlotType) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}
