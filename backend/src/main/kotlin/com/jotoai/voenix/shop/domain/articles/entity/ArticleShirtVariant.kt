package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.dto.ArticleShirtVariantDto
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
@Table(name = "article_shirt_variants")
data class ArticleShirtVariant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: Article,
    @Column(nullable = false)
    var color: String,
    @Column(nullable = false, length = 50)
    var size: String,
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
        ArticleShirtVariantDto(
            id = requireNotNull(this.id) { "ArticleShirtVariant ID cannot be null when converting to DTO" },
            articleId = this.article.id!!,
            color = this.color,
            size = this.size,
            exampleImageUrl = this.exampleImageFilename?.let { "/images/articles/shirts/variant-example-images/$it" },
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
