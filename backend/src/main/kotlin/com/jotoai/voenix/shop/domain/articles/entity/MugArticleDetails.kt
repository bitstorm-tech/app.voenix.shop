package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.MugArticleDetailsDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "article_mug_details")
class MugArticleDetails(
    @Id
    @Column(name = "article_id")
    var articleId: Long,
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
    var createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        MugArticleDetailsDto(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MugArticleDetails) return false
        return articleId == other.articleId
    }

    override fun hashCode(): Int = articleId.hashCode()
}
