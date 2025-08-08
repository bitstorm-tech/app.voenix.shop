package com.jotoai.voenix.shop.article.internal.entity

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
class MugArticleVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
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
    var createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MugArticleVariant) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
