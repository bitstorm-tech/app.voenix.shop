package com.jotoai.voenix.shop.article.internal.entity

import com.jotoai.voenix.shop.article.api.enums.FitType
import com.jotoai.voenix.shop.article.internal.dto.ShirtArticleDetailsDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "article_shirt_details")
@Suppress("LongParameterList")
class ShirtArticleDetails(
    @Id
    @Column(name = "article_id")
    var articleId: Long,
    @Column(nullable = false)
    var material: String,
    @Column(name = "care_instructions", columnDefinition = "TEXT")
    var careInstructions: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "fit_type", nullable = false, length = 50)
    var fitType: FitType,
    @Column(name = "available_sizes", nullable = false, columnDefinition = "text[]")
    var availableSizes: Array<String>,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    var createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ShirtArticleDetailsDto(
            articleId = this.articleId,
            material = this.material,
            careInstructions = this.careInstructions,
            fitType = this.fitType,
            availableSizes = this.availableSizes.toList(),
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShirtArticleDetails) return false
        return articleId == other.articleId
    }

    override fun hashCode(): Int = articleId.hashCode()
}
