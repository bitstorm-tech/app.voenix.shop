package com.jotoai.voenix.shop.mugs.entity

import com.jotoai.voenix.shop.mugs.dto.MugCategoryDto
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "mug_categories")
data class MugCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null
)

fun MugCategory.toDto(): MugCategoryDto = MugCategoryDto(
    id = requireNotNull(this.id) { "MugCategory ID cannot be null when converting to DTO" },
    name = this.name,
    description = this.description,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)