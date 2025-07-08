package com.jotoai.voenix.shop.prompts.entity

import com.jotoai.voenix.shop.prompts.dto.PromptCategoryDto
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
data class PromptCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    val prompts: List<Prompt> = emptyList(),
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
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
