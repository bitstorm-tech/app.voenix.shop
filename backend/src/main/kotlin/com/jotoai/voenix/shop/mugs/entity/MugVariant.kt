package com.jotoai.voenix.shop.mugs.entity

import com.jotoai.voenix.shop.mugs.dto.MugVariantDto
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
@Table(name = "mug_variants")
data class MugVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "mug_id", nullable = false)
    var mugId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mug_id", insertable = false, updatable = false)
    var mug: Mug? = null,
    @Column(name = "color_code", nullable = false)
    var colorCode: String,
    @Column(name = "example_image_filename", nullable = false)
    var exampleImageFilename: String,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        MugVariantDto(
            id = requireNotNull(this.id) { "MugVariant ID cannot be null when converting to DTO" },
            mugId = this.mugId,
            colorCode = this.colorCode,
            exampleImageUrl = "/images/${this.exampleImageFilename}",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
