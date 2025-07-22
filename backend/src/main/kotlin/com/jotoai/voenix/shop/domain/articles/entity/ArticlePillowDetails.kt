package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.ArticlePillowDetailsDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "article_pillow_details")
data class ArticlePillowDetails(
    @Id
    @Column(name = "article_id")
    val articleId: Long,
    @Column(name = "width_cm", nullable = false)
    var widthCm: Int,
    @Column(name = "height_cm", nullable = false)
    var heightCm: Int,
    @Column(name = "depth_cm", nullable = false)
    var depthCm: Int,
    @Column(nullable = false)
    var material: String,
    @Column(name = "filling_type", nullable = false)
    var fillingType: String,
    @Column(name = "cover_removable", nullable = false)
    var coverRemovable: Boolean = true,
    @Column(nullable = false)
    var washable: Boolean = true,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ArticlePillowDetailsDto(
            articleId = this.articleId,
            widthCm = this.widthCm,
            heightCm = this.heightCm,
            depthCm = this.depthCm,
            material = this.material,
            fillingType = this.fillingType,
            coverRemovable = this.coverRemovable,
            washable = this.washable,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
