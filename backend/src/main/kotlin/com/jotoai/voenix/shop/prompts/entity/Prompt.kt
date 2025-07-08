package com.jotoai.voenix.shop.prompts.entity

import com.jotoai.voenix.shop.prompts.dto.PromptDto
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
@Table(name = "prompts")
data class Prompt(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, length = 500)
    var title: String,
    @Column(columnDefinition = "TEXT")
    var content: String? = null,
    @Column(name = "category_id")
    var categoryId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    var category: PromptCategory? = null,
    @Column(nullable = false)
    var active: Boolean = true,
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
            content = this.content,
            categoryId = this.categoryId,
            category = this.category?.toDto(),
            active = this.active,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
