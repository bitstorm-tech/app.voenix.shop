package com.jotoai.voenix.shop.domain.prompts.entity

import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.dto.PublicPromptDto
import jakarta.persistence.CascadeType
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
@Table(name = "prompts")
data class Prompt(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, length = 500)
    var title: String,
    @Column(name = "prompt_text", columnDefinition = "TEXT")
    var promptText: String? = null,
    @Column(name = "category_id")
    var categoryId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: PromptCategory? = null,
    @Column(name = "subcategory_id")
    var subcategoryId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", insertable = false, updatable = false)
    var subcategory: PromptSubCategory? = null,
    @Column(nullable = false)
    var active: Boolean = true,
    @Column(name = "example_image_filename", length = 500)
    var exampleImageFilename: String? = null,
    @OneToMany(mappedBy = "prompt", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var promptSlotVariantMappings: MutableList<PromptSlotVariantMapping> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        PromptDto(
            id = requireNotNull(this.id) { "Prompt ID cannot be null when converting to DTO" },
            title = this.title,
            promptText = this.promptText,
            categoryId = this.categoryId,
            category = this.category?.toDto(),
            subcategoryId = this.subcategoryId,
            subcategory = this.subcategory?.toDto(),
            active = this.active,
            slots =
                this.promptSlotVariantMappings
                    .sortedBy {
                        it.promptSlotVariant.promptSlotType?.position ?: 0
                    }.map {
                        it.promptSlotVariant
                            .toDto()
                    },
            exampleImageUrl = this.exampleImageFilename?.let { "/images/prompt-example-images/$it" },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun addPromptSlotVariant(promptSlotVariant: PromptSlotVariant) {
        val mapping =
            PromptSlotVariantMapping(
                id = PromptSlotVariantMappingId(this.id ?: 0, promptSlotVariant.id ?: 0),
                prompt = this,
                promptSlotVariant = promptSlotVariant,
            )
        this.promptSlotVariantMappings.add(mapping)
    }

    fun removePromptSlotVariant(promptSlotVariant: PromptSlotVariant) {
        this.promptSlotVariantMappings.removeIf { it.promptSlotVariant.id == promptSlotVariant.id }
    }

    fun clearPromptSlotVariants() {
        this.promptSlotVariantMappings.clear()
    }

    fun toPublicDto() =
        PublicPromptDto(
            id = requireNotNull(this.id) { "Prompt ID cannot be null when converting to DTO" },
            title = this.title,
            exampleImageUrl = this.exampleImageFilename?.let { "/images/prompt-example-images/$it" },
            category = this.category?.toPublicDto(),
            subcategory = this.subcategory?.toPublicDto(),
            slots =
                this.promptSlotVariantMappings
                    .sortedBy {
                        it.promptSlotVariant.promptSlotType?.position ?: 0
                    }.map {
                        it.promptSlotVariant
                            .toPublicDto()
                    },
        )
}
