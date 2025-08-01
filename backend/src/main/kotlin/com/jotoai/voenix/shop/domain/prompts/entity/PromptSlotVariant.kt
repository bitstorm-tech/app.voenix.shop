package com.jotoai.voenix.shop.domain.prompts.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "prompt_slot_variants")
class PromptSlotVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "slot_type_id", nullable = false)
    var promptSlotTypeId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_type_id", insertable = false, updatable = false)
    var promptSlotType: PromptSlotType? = null,
    @Column(nullable = false, unique = true, length = 255)
    var name: String,
    @Column(nullable = true, columnDefinition = "TEXT")
    var prompt: String? = null,
    @Column(nullable = true, columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "example_image_filename", length = 500)
    var exampleImageFilename: String? = null,
    @OneToMany(mappedBy = "promptSlotVariant", fetch = FetchType.LAZY)
    var promptSlotVariantMappings: MutableList<PromptSlotVariantMapping> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptSlotVariant) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
