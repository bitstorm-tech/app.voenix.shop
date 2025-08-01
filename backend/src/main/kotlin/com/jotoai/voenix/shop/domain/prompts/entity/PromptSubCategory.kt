package com.jotoai.voenix.shop.domain.prompts.entity

import com.jotoai.voenix.shop.domain.prompts.dto.PromptSubCategoryDto
import com.jotoai.voenix.shop.domain.prompts.dto.PublicPromptSubCategoryDto
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
@Table(name = "prompt_subcategories")
class PromptSubCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_category_id", nullable = false)
    var promptCategory: PromptCategory,
    @Column(nullable = false)
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @OneToMany(mappedBy = "subcategory", fetch = FetchType.LAZY)
    val prompts: List<Prompt> = emptyList(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        PromptSubCategoryDto(
            id = requireNotNull(this.id) { "PromptSubCategory ID cannot be null when converting to DTO" },
            promptCategoryId = requireNotNull(this.promptCategory.id) { "PromptCategory ID cannot be null when converting to DTO" },
            name = this.name,
            description = this.description,
            promptsCount = this.prompts.size,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    fun toPublicDto() =
        PublicPromptSubCategoryDto(
            id = requireNotNull(this.id) { "PromptSubCategory ID cannot be null when converting to DTO" },
            name = this.name,
            description = this.description,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PromptSubCategory) return false
        return promptCategory.id == other.promptCategory.id && name == other.name
    }

    override fun hashCode(): Int {
        var result = promptCategory.id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        return result
    }
}
