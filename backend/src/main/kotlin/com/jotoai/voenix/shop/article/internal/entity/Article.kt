package com.jotoai.voenix.shop.article.internal.entity

import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleSubCategory
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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "articles")
@Suppress("LongParameterList")
class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    var name: String,
    @Column(name = "description_short", nullable = false, columnDefinition = "TEXT")
    var descriptionShort: String,
    @Column(name = "description_long", nullable = false, columnDefinition = "TEXT")
    var descriptionLong: String,
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
    @Column(name = "supplier_id")
    var supplierId: Long? = null,
    @Column(name = "supplier_article_name")
    var supplierArticleName: String? = null,
    @Column(name = "supplier_article_number")
    var supplierArticleNumber: String? = null,
    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    var mugVariants: MutableList<MugArticleVariant> = mutableListOf(),
    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    var shirtVariants: MutableList<ShirtArticleVariant> = mutableListOf(),
    @OneToOne(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var costCalculation: CostCalculation? = null,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    var updatedAt: OffsetDateTime? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Article) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: javaClass.hashCode()
}
