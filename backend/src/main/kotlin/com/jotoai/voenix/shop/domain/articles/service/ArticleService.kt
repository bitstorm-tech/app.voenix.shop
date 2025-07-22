package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleSubCategoryRepository
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.ArticleVariant
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.enums.VariantType
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.ArticleVariantRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val articleVariantRepository: ArticleVariantRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
    private val articleSubCategoryRepository: ArticleSubCategoryRepository,
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

        // Validate type-specific details are provided
        validateTypeSpecificDetails(request.articleType, request)

        // Create article
        val article =
            Article(
                name = request.name,
                descriptionShort = request.descriptionShort,
                descriptionLong = request.descriptionLong,
                exampleImageFilename = request.exampleImageFilename,
                price = request.price,
                active = request.active,
                articleType = request.articleType,
                category = category,
                subcategory = subcategory,
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

        // Create variants
        request.variants.forEach { variantRequest ->
            createVariant(savedArticle, variantRequest)
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

        // Update article
        article.apply {
            name = request.name
            descriptionShort = request.descriptionShort
            descriptionLong = request.descriptionLong
            exampleImageFilename = request.exampleImageFilename
            price = request.price
            active = request.active
            this.category = category
            this.subcategory = subcategory
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

    @Transactional
    fun createVariant(
        articleId: Long,
        request: CreateArticleVariantRequest,
    ): ArticleVariantDto {
        val article =
            articleRepository
                .findById(articleId)
                .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

        return createVariant(article, request).toDto()
    }

    @Transactional
    fun updateVariant(
        variantId: Long,
        request: CreateArticleVariantRequest,
    ): ArticleVariantDto {
        val variant =
            articleVariantRepository
                .findById(variantId)
                .orElseThrow { ResourceNotFoundException("Variant not found with id: $variantId") }

        variant.apply {
            variantType = VariantType.valueOf(request.variantType)
            variantValue = request.variantValue
            sku = request.sku
            exampleImageFilename = request.exampleImageFilename
        }

        return articleVariantRepository.save(variant).toDto()
    }

    @Transactional
    fun deleteVariant(variantId: Long) {
        if (!articleVariantRepository.existsById(variantId)) {
            throw ResourceNotFoundException("Variant not found with id: $variantId")
        }
        articleVariantRepository.deleteById(variantId)
    }

    private fun createVariant(
        article: Article,
        request: CreateArticleVariantRequest,
    ): ArticleVariant {
        // Validate SKU uniqueness if provided
        request.sku?.let {
            if (articleVariantRepository.existsBySku(it)) {
                throw IllegalArgumentException("SKU already exists: $it")
            }
        }

        val variant =
            ArticleVariant(
                article = article,
                variantType = VariantType.valueOf(request.variantType),
                variantValue = request.variantValue,
                sku = request.sku,
                exampleImageFilename = request.exampleImageFilename,
            )

        return articleVariantRepository.save(variant)
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
            exampleImageFilename = article.exampleImageFilename,
            price = article.price,
            active = article.active,
            articleType = article.articleType,
            categoryId = article.category.id!!,
            categoryName = article.category.name,
            subcategoryId = article.subcategory?.id,
            subcategoryName = article.subcategory?.name,
            variants = article.variants.map { it.toDto() },
            mugDetails = mugDetails,
            shirtDetails = shirtDetails,
            pillowDetails = pillowDetails,
            createdAt = article.createdAt,
            updatedAt = article.updatedAt,
        )
    }
}
