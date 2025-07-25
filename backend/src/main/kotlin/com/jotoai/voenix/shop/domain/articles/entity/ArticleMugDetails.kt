package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.ArticleMugDetailsDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "article_mug_details")
data class ArticleMugDetails(
    @Id
    @Column(name = "article_id")
    val articleId: Long,
    @Column(name = "height_mm", nullable = false)
    var heightMm: Int,
    @Column(name = "diameter_mm", nullable = false)
    var diameterMm: Int,
    @Column(name = "print_template_width_mm", nullable = false)
    var printTemplateWidthMm: Int,
    @Column(name = "print_template_height_mm", nullable = false)
    var printTemplateHeightMm: Int,
    @Column(name = "filling_quantity", length = 50)
    var fillingQuantity: String? = null,
    @Column(name = "dishwasher_safe", nullable = false)
    var dishwasherSafe: Boolean = true,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ArticleMugDetailsDto(
            articleId = this.articleId,
            heightMm = this.heightMm,
            diameterMm = this.diameterMm,
            printTemplateWidthMm = this.printTemplateWidthMm,
            printTemplateHeightMm = this.printTemplateHeightMm,
            fillingQuantity = this.fillingQuantity,
            dishwasherSafe = this.dishwasherSafe,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
