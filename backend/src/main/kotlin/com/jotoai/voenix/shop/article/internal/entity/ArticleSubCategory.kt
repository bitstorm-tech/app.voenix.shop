package com.jotoai.voenix.shop.article.internal.entity

import com.jotoai.voenix.shop.article.internal.dto.ArticleSubCategoryDto
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
@Table(name = "article_sub_categories")
class ArticleSubCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_category_id", nullable = false)
    var articleCategory: ArticleCategory,
    @Column(nullable = false)
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ArticleSubCategoryDto(
            id =
                requireNotNull(this.id) {
                    "ArticleSubCategory ID cannot be null when converting to DTO"
                },
            articleCategoryId =
                requireNotNull(this.articleCategory.id) {
                    "ArticleCategory ID cannot be null when converting to DTO"
                },
            name = this.name,
            description = this.description,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArticleSubCategory) return false
        return articleCategory.id == other.articleCategory.id && name == other.name
    }

    override fun hashCode(): Int {
        var result = articleCategory.id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        return result
    }
}
