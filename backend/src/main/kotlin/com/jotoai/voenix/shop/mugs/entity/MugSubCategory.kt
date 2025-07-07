package com.jotoai.voenix.shop.mugs.entity

import com.jotoai.voenix.shop.mugs.dto.MugSubCategoryDto
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "mug_sub_categories")
data class MugSubCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mug_category_id", nullable = false)
    var mugCategory: MugCategory,
    
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
) {
    fun toDto() = MugSubCategoryDto(
        id = requireNotNull(this.id) { "MugSubCategory ID cannot be null when converting to DTO" },
        mugCategoryId = requireNotNull(this.mugCategory.id) { "MugCategory ID cannot be null when converting to DTO" },
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
