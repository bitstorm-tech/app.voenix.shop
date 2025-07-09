package com.jotoai.voenix.shop.prompts.entity

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
data class PromptSlotId(
    @Column(name = "prompt_id")
    val promptId: Long = 0,
    @Column(name = "slot_id")
    val slotId: Long = 0,
) : Serializable

@Entity
@Table(name = "prompt_slots")
data class PromptSlot(
    @EmbeddedId
    val id: PromptSlotId = PromptSlotId(),
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("promptId")
    @JoinColumn(name = "prompt_id")
    val prompt: Prompt,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("slotId")
    @JoinColumn(name = "slot_id")
    val slot: Slot,
    @Column(nullable = false)
    var position: Int = 0,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
)
