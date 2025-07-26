package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleSubCategoryRepository
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleMugVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticlePillowVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleShirtVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.ArticleMugVariant
import com.jotoai.voenix.shop.domain.articles.entity.ArticlePillowVariant
import com.jotoai.voenix.shop.domain.articles.entity.ArticleShirtVariant
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.repository.ArticleMugVariantRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticlePillowVariantRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleShirtVariantRepository
import com.jotoai.voenix.shop.domain.suppliers.repository.SupplierRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val articleMugVariantRepository: ArticleMugVariantRepository,
    private val articleShirtVariantRepository: ArticleShirtVariantRepository,
    private val articlePillowVariantRepository: ArticlePillowVariantRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
    private val articleSubCategoryRepository: ArticleSubCategoryRepository,
    private val supplierRepository: SupplierRepository,
    private val mugDetailsService: MugDetailsService,
    private val shirtDetailsService: ShirtDetailsService,
    private val pillowDetailsService: PillowDetailsService,
) {
    @Transactional(readOnly = true)
    fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType? = null,
        categoryId: Long? = null,
        subcategoryId: Long? = null,
        active: Boolean? = null,
        search: String? = null,
    ): PaginatedResponse<ArticleDto> {
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val articlesPage =
            articleRepository.findAllWithFilters(
                articleType = articleType,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                active = active,
                pageable = pageable,
            )

        return PaginatedResponse(
            content = articlesPage.content.map { it.toDto() },
            currentPage = articlesPage.number,
            totalPages = articlesPage.totalPages,
            totalElements = articlesPage.totalElements,
            size = articlesPage.size,
        )
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ArticleWithDetailsDto {
        val article =
            articleRepository.findByIdWithDetails(id)
                ?: throw ResourceNotFoundException("Article not found with id: $id")

        return buildArticleWithDetailsDto(article)
    }

    @Transactional
    fun create(request: CreateArticleRequest): ArticleWithDetailsDto {
        // Validate category exists
        val category =
            articleCategoryRepository
                .findById(request.categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: ${request.categoryId}") }

        // Validate subcategory if provided
        val subcategory =
            request.subcategoryId?.let {
                articleSubCategoryRepository
                    .findById(it)
                    .orElseThrow { ResourceNotFoundException("Subcategory not found with id: $it") }
            }

        // Validate supplier exists if provided
        val supplier =
            request.supplierId?.let { supplierId ->
                supplierRepository
                    .findById(supplierId)
                    .orElseThrow { ResourceNotFoundException("Supplier not found with id: $supplierId") }
            }

        // Validate type-specific details are provided
        validateTypeSpecificDetails(request.articleType, request)

        // Create article
        val article =
            Article(
                name = request.name,
                descriptionShort = request.descriptionShort,
                descriptionLong = request.descriptionLong,
                active = request.active,
                articleType = request.articleType,
                category = category,
                subcategory = subcategory,
                supplier = supplier,
            )

        val savedArticle = articleRepository.save(article)

        // Create type-specific details
        when (request.articleType) {
            ArticleType.MUG ->
                request.mugDetails?.let {
                    mugDetailsService.create(savedArticle, it)
                }
            ArticleType.SHIRT ->
                request.shirtDetails?.let {
                    shirtDetailsService.create(savedArticle, it)
                }
            ArticleType.PILLOW ->
                request.pillowDetails?.let {
                    pillowDetailsService.create(savedArticle, it)
                }
        }

        // Create type-specific variants
        when (request.articleType) {
            ArticleType.MUG ->
                request.mugVariants?.forEach { variantRequest ->
                    createMugVariant(savedArticle, variantRequest)
                }
            ArticleType.SHIRT ->
                request.shirtVariants?.forEach { variantRequest ->
                    createShirtVariant(savedArticle, variantRequest)
                }
            ArticleType.PILLOW ->
                request.pillowVariants?.forEach { variantRequest ->
                    createPillowVariant(savedArticle, variantRequest)
                }
        }

        return findById(savedArticle.id!!)
    }

    @Transactional
    fun update(
        id: Long,
        request: UpdateArticleRequest,
    ): ArticleWithDetailsDto {
        val article =
            articleRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $id") }

        // Validate category exists
        val category =
            articleCategoryRepository
                .findById(request.categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: ${request.categoryId}") }

        // Validate subcategory if provided
        val subcategory =
            request.subcategoryId?.let {
                articleSubCategoryRepository
                    .findById(it)
                    .orElseThrow { ResourceNotFoundException("Subcategory not found with id: $it") }
            }

        // Validate supplier exists if provided
        val supplier =
            request.supplierId?.let { supplierId ->
                supplierRepository
                    .findById(supplierId)
                    .orElseThrow { ResourceNotFoundException("Supplier not found with id: $supplierId") }
            }

        // Update article
        article.apply {
            name = request.name
            descriptionShort = request.descriptionShort
            descriptionLong = request.descriptionLong
            active = request.active
            this.category = category
            this.subcategory = subcategory
            this.supplier = supplier
        }

        val updatedArticle = articleRepository.save(article)

        // Update type-specific details
        when (article.articleType) {
            ArticleType.MUG ->
                request.mugDetails?.let {
                    mugDetailsService.update(updatedArticle, it)
                }
            ArticleType.SHIRT ->
                request.shirtDetails?.let {
                    shirtDetailsService.update(updatedArticle, it)
                }
            ArticleType.PILLOW ->
                request.pillowDetails?.let {
                    pillowDetailsService.update(updatedArticle, it)
                }
        }

        return findById(updatedArticle.id!!)
    }

    @Transactional
    fun delete(id: Long) {
        if (!articleRepository.existsById(id)) {
            throw ResourceNotFoundException("Article not found with id: $id")
        }
        articleRepository.deleteById(id)
    }

    private fun createMugVariant(
        article: Article,
        request: CreateArticleMugVariantRequest,
    ): ArticleMugVariant {
        val variant =
            ArticleMugVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageFilename = request.exampleImageFilename,
            )

        return articleMugVariantRepository.save(variant)
    }

    private fun createShirtVariant(
        article: Article,
        request: CreateArticleShirtVariantRequest,
    ): ArticleShirtVariant {
        val variant =
            ArticleShirtVariant(
                article = article,
                color = request.color,
                size = request.size,
                exampleImageFilename = request.exampleImageFilename,
            )

        return articleShirtVariantRepository.save(variant)
    }

    private fun createPillowVariant(
        article: Article,
        request: CreateArticlePillowVariantRequest,
    ): ArticlePillowVariant {
        val variant =
            ArticlePillowVariant(
                article = article,
                color = request.color,
                material = request.material,
                exampleImageFilename = request.exampleImageFilename,
            )

        return articlePillowVariantRepository.save(variant)
    }

    private fun validateTypeSpecificDetails(
        type: ArticleType,
        request: CreateArticleRequest,
    ) {
        when (type) {
            ArticleType.MUG ->
                require(request.mugDetails != null) {
                    "Mug details are required for MUG article type"
                }
            ArticleType.SHIRT ->
                require(request.shirtDetails != null) {
                    "Shirt details are required for SHIRT article type"
                }
            ArticleType.PILLOW ->
                require(request.pillowDetails != null) {
                    "Pillow details are required for PILLOW article type"
                }
        }
    }

    private fun buildArticleWithDetailsDto(article: Article): ArticleWithDetailsDto {
        val mugDetails =
            if (article.articleType == ArticleType.MUG) {
                mugDetailsService.findByArticleId(article.id!!)
            } else {
                null
            }

        val shirtDetails =
            if (article.articleType == ArticleType.SHIRT) {
                shirtDetailsService.findByArticleId(article.id!!)
            } else {
                null
            }

        val pillowDetails =
            if (article.articleType == ArticleType.PILLOW) {
                pillowDetailsService.findByArticleId(article.id!!)
            } else {
                null
            }

        return ArticleWithDetailsDto(
            id = article.id!!,
            name = article.name,
            descriptionShort = article.descriptionShort,
            descriptionLong = article.descriptionLong,
            active = article.active,
            articleType = article.articleType,
            categoryId = article.category.id!!,
            categoryName = article.category.name,
            subcategoryId = article.subcategory?.id,
            subcategoryName = article.subcategory?.name,
            supplierId = article.supplier?.id,
            supplierName = article.supplier?.name,
            mugVariants = if (article.articleType == ArticleType.MUG) article.mugVariants.map { it.toDto() } else null,
            shirtVariants = if (article.articleType == ArticleType.SHIRT) article.shirtVariants.map { it.toDto() } else null,
            pillowVariants = if (article.articleType == ArticleType.PILLOW) article.pillowVariants.map { it.toDto() } else null,
            mugDetails = mugDetails,
            shirtDetails = shirtDetails,
            pillowDetails = pillowDetails,
            createdAt = article.createdAt,
            updatedAt = article.updatedAt,
        )
    }
}
