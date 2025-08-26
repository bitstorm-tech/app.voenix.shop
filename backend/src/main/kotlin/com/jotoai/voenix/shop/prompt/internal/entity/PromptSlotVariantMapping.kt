package com.jotoai.voenix.shop.prompt.internal.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.time.OffsetDateTime

@Embeddable
data class PromptSlotVariantMappingId(
    @Column(name = "prompt_id")
    val promptId: Long = 0,
    @Column(name = "slot_id")
    val promptSlotVariantId: Long = 0,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

@Entity
@Table(name = "prompt_slot_variant_mappings")
class PromptSlotVariantMapping(
    @EmbeddedId
    val id: PromptSlotVariantMappingId = PromptSlotVariantMappingId(),
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promptId")
    @JoinColumn(name = "prompt_id")
    val prompt: Prompt,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promptSlotVariantId")
    @JoinColumn(name = "slot_id")
    val promptSlotVariant: PromptSlotVariant,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptSlotVariantMapping) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
