package com.jotoai.voenix.shop.domain.articles.entity

import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleSubCategory
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.suppliers.entity.Supplier
import jakarta.persistence.CascadeType
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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "articles")
data class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    var name: String,
    @Column(name = "description_short", nullable = false, columnDefinition = "TEXT")
    var descriptionShort: String,
    @Column(name = "description_long", nullable = false, columnDefinition = "TEXT")
    var descriptionLong: String,
    @Column(name = "example_image_filename", nullable = false, length = 500)
    var exampleImageFilename: String,
    @Column(nullable = false)
    var price: Int,
    @Column(nullable = false)
    var active: Boolean = true,
    @Enumerated(EnumType.STRING)
    @Column(name = "article_type", nullable = false, length = 50)
    var articleType: ArticleType,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: ArticleCategory,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    var subcategory: ArticleSubCategory? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    var supplier: Supplier? = null,
    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    var mugVariants: MutableList<ArticleMugVariant> = mutableListOf(),
    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    var shirtVariants: MutableList<ArticleShirtVariant> = mutableListOf(),
    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    var pillowVariants: MutableList<ArticlePillowVariant> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    fun toDto() =
        ArticleDto(
            id = requireNotNull(this.id) { "Article ID cannot be null when converting to DTO" },
            name = this.name,
            descriptionShort = this.descriptionShort,
            descriptionLong = this.descriptionLong,
            exampleImageFilename = this.exampleImageFilename,
            price = this.price,
            active = this.active,
            articleType = this.articleType,
            categoryId = this.category.id!!,
            categoryName = this.category.name,
            subcategoryId = this.subcategory?.id,
            subcategoryName = this.subcategory?.name,
            supplierId = this.supplier?.id,
            supplierName = this.supplier?.name,
            mugVariants = if (this.articleType == ArticleType.MUG) this.mugVariants.map { it.toDto() } else null,
            shirtVariants = if (this.articleType == ArticleType.SHIRT) this.shirtVariants.map { it.toDto() } else null,
            pillowVariants = if (this.articleType == ArticleType.PILLOW) this.pillowVariants.map { it.toDto() } else null,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
        )
}
