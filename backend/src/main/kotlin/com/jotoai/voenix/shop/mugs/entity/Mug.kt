package com.jotoai.voenix.shop.mugs.entity

import com.jotoai.voenix.shop.mugs.dto.MugDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "mugs")
data class Mug(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @Column(name = "description_long", nullable = false, columnDefinition = "TEXT")
    var descriptionLong: String,
    @Column(name = "description_short", nullable = false, columnDefinition = "TEXT")
    var descriptionShort: String,
    @Column(nullable = false, length = 500)
    var image: String,
    @Column(nullable = false)
    var price: Int,
    @Column(name = "height_mm", nullable = false)
    var heightMm: Int,
    @Column(name = "diameter_mm", nullable = false)
    var diameterMm: Int,
    @Column(name = "print_template_width_mm", nullable = false)
    var printTemplateWidthMm: Int,
    @Column(name = "print_template_height_mm", nullable = false)
    var printTemplateHeightMm: Int,
    @Column(name = "filling_quantity")
    var fillingQuantity: String? = null,
    @Column(name = "dishwasher_safe", nullable = false)
    var dishwasherSafe: Boolean = true,
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
        MugDto(
            id = requireNotNull(this.id) { "Mug ID cannot be null when converting to DTO" },
            name = this.name,
            descriptionLong = this.descriptionLong,
            descriptionShort = this.descriptionShort,
            image = this.image,
            price = this.price,
            heightMm = this.heightMm,
            diameterMm = this.diameterMm,
            printTemplateWidthMm = this.printTemplateWidthMm,
            printTemplateHeightMm = this.printTemplateHeightMm,
            fillingQuantity = this.fillingQuantity,
            dishwasherSafe = this.dishwasherSafe,
            active = this.active,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
