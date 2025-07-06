package com.jotoai.voenix.shop.prompts.entity

import com.jotoai.voenix.shop.prompts.dto.PromptDto
import jakarta.persistence.*
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
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null
)

fun Prompt.toDto(): PromptDto = PromptDto(
    id = requireNotNull(this.id) { "Prompt ID cannot be null when converting to DTO" },
    title = this.title,
    content = this.content,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)