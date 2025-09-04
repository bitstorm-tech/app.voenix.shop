package com.jotoai.voenix.shop.prompt.internal.entity

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptCategoryDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "prompt_categories")
class PromptCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    val prompts: List<Prompt> = emptyList(),
    @OneToMany(mappedBy = "promptCategory", fetch = FetchType.LAZY)
    val subcategories: List<PromptSubCategory> = emptyList(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        PromptCategoryDto(
            id = requireNotNull(this.id) { "PromptCategory ID cannot be null when converting to DTO" },
            name = this.name,
            promptsCount = this.prompts.size,
            subcategoriesCount = this.subcategories.size,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun toPublicDto() =
        PublicPromptCategoryDto(
            id = requireNotNull(this.id) { "PromptCategory ID cannot be null when converting to DTO" },
            name = this.name,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptCategory) return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}
