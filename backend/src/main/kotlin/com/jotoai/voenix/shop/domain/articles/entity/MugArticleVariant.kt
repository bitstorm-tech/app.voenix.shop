package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
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
@Table(name = "article_mug_variants")
data class MugArticleVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article,
    @Column(name = "inside_color_code", nullable = false)
    var insideColorCode: String = "#ffffff",
    @Column(name = "outside_color_code", nullable = false)
    var outsideColorCode: String = "#ffffff",
    @Column(nullable = false)
    var name: String,
    @Column(name = "example_image_filename", length = 500)
    var exampleImageFilename: String? = null,
    @Column(name = "article_variant_number", length = 100)
    var articleVariantNumber: String? = null,
    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        MugArticleVariantDto(
            id = requireNotNull(this.id) { "MugArticleVariant ID cannot be null when converting to DTO" },
            articleId = this.article.id!!,
            insideColorCode = this.insideColorCode,
            outsideColorCode = this.outsideColorCode,
            name = this.name,
            exampleImageUrl = this.exampleImageFilename?.let { "/images/articles/mugs/variant-example-images/$it" },
            articleVariantNumber = this.articleVariantNumber,
            isDefault = this.isDefault,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
