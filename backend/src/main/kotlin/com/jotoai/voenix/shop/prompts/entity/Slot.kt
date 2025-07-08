package com.jotoai.voenix.shop.prompts.entity

import com.jotoai.voenix.shop.prompts.dto.SlotDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "slots")
data class Slot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "slot_type_id", nullable = false)
    var slotTypeId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_type_id", insertable = false, updatable = false)
    var slotType: SlotType? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var prompt: String,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        SlotDto(
            id = requireNotNull(this.id) { "Slot ID cannot be null when converting to DTO" },
            slotTypeId = this.slotTypeId,
            slotType = this.slotType?.toDto(),
            name = this.name,
            prompt = this.prompt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
