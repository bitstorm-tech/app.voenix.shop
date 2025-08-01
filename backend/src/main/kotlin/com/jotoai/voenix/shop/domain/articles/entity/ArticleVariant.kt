package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.ArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.enums.VariantType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
@Table(name = "article_variants")
class ArticleVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article,
    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type", nullable = false, length = 50)
    var variantType: VariantType,
    @Column(name = "variant_value", nullable = false)
    var variantValue: String,
    @Column(unique = true)
    var sku: String? = null,
    @Column(name = "example_image_filename", length = 500)
    var exampleImageFilename: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ArticleVariantDto(
            id = requireNotNull(this.id) { "ArticleVariant ID cannot be null when converting to DTO" },
            articleId = this.article.id!!,
            variantType = this.variantType,
            variantValue = this.variantValue,
            sku = this.sku,
            exampleImageUrl = this.exampleImageFilename?.let { "/images/$it" },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArticleVariant) return false
        return sku != null && sku == other.sku
    }

    override fun hashCode(): Int = sku?.hashCode() ?: 0
}
