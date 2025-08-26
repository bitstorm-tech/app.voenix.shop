package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.ArticleFacade
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.CreateArticleRequest
import com.jotoai.voenix.shop.article.api.dto.CreateCostCalculationRequest
import com.jotoai.voenix.shop.article.api.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugVariantDto
import com.jotoai.voenix.shop.article.api.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.article.api.dto.UpdateCostCalculationRequest
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.internal.config.ArticleServiceDependencies
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.CostCalculation
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleServiceImpl(
    private val dependencies: ArticleServiceDependencies,
) : ArticleQueryService,
    ArticleFacade {
    companion object {
        private const val CENTS_TO_EUROS = 100.0
    }

    @Transactional(readOnly = true)
    override fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType?,
        categoryId: Long?,
        subcategoryId: Long?,
        active: Boolean?,
    ): ArticlePaginatedResponse<ArticleDto> {
        val pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val articlesPage =
            dependencies.articleRepository.findAllWithFilters(
                articleType = articleType,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                active = active,
                pageable = pageable,
            )

        return ArticlePaginatedResponse(
            content = articlesPage.content.map { dependencies.articleAssembler.toDto(it) },
            currentPage = articlesPage.number,
            totalPages = articlesPage.totalPages,
            totalElements = articlesPage.totalElements,
            size = articlesPage.size,
        )
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): ArticleWithDetailsDto {
        // First, fetch article with basic details to determine the type
        val articleBasic =
            dependencies.articleRepository.findByIdWithBasicDetails(id)
                ?: throw ArticleNotFoundException("Article not found with id: $id")

        // Then fetch with appropriate variants based on article type
        val article =
            when (articleBasic.articleType) {
                ArticleType.MUG -> dependencies.articleRepository.findMugByIdWithDetails(id)
                ArticleType.SHIRT -> dependencies.articleRepository.findShirtByIdWithDetails(id)
            } ?: throw ArticleNotFoundException("Article not found with id: $id")

        return buildArticleWithDetailsDto(article)
    }

    @Transactional
    override fun create(request: CreateArticleRequest): ArticleWithDetailsDto {
        val category =
            dependencies.articleCategoryRepository
                .findById(request.categoryId)
                .orElseThrow { ArticleNotFoundException("Category not found with id: ${request.categoryId}") }
        val subcategory =
            request.subcategoryId?.let {
                dependencies.articleSubCategoryRepository
                    .findById(it)
                    .orElseThrow { ArticleNotFoundException("Subcategory not found with id: $it") }
            }

        // Validate supplier exists if provided
        request.supplierId?.let { supplierId ->
            if (!dependencies.supplierService.existsById(supplierId)) {
                throw ArticleNotFoundException("Supplier not found with id: $supplierId")
            }
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
                supplierId = request.supplierId,
                supplierArticleName = request.supplierArticleName,
                supplierArticleNumber = request.supplierArticleNumber,
            )

        val savedArticle = dependencies.articleRepository.save(article)

        // Create type-specific details
        when (request.articleType) {
            ArticleType.MUG ->
                request.mugDetails?.let {
                    dependencies.mugDetailsService.create(savedArticle, it)
                }
            ArticleType.SHIRT ->
                request.shirtDetails?.let {
                    dependencies.shirtDetailsService.create(savedArticle, it)
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
        }

        // Create cost calculation if provided
        request.costCalculation?.let { costCalcRequest ->
            createCostCalculation(savedArticle, costCalcRequest)
        }

        return findById(savedArticle.id!!)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateArticleRequest,
    ): ArticleWithDetailsDto {
        val article =
            dependencies.articleRepository
                .findById(id)
                .orElseThrow { ArticleNotFoundException("Article not found with id: $id") }

        // Validate category exists
        val category =
            dependencies.articleCategoryRepository
                .findById(request.categoryId)
                .orElseThrow { ArticleNotFoundException("Category not found with id: ${request.categoryId}") }

        // Validate subcategory if provided
        val subcategory =
            request.subcategoryId?.let {
                dependencies.articleSubCategoryRepository
                    .findById(it)
                    .orElseThrow { ArticleNotFoundException("Subcategory not found with id: $it") }
            }

        // Validate supplier exists if provided
        request.supplierId?.let { supplierId ->
            if (!dependencies.supplierService.existsById(supplierId)) {
                throw ArticleNotFoundException("Supplier not found with id: $supplierId")
            }
        }

        // Update article
        article.apply {
            name = request.name
            descriptionShort = request.descriptionShort
            descriptionLong = request.descriptionLong
            active = request.active
            this.category = category
            this.subcategory = subcategory
            this.supplierId = request.supplierId
            this.supplierArticleName = request.supplierArticleName
            this.supplierArticleNumber = request.supplierArticleNumber
        }

        val updatedArticle = dependencies.articleRepository.save(article)

        // Update type-specific details
        when (article.articleType) {
            ArticleType.MUG ->
                request.mugDetails?.let {
                    dependencies.mugDetailsService.update(updatedArticle, it)
                }
            ArticleType.SHIRT ->
                request.shirtDetails?.let {
                    dependencies.shirtDetailsService.update(updatedArticle, it)
                }
        }

        // Update cost calculation if provided
        request.costCalculation?.let { costCalcRequest ->
            updateCostCalculation(updatedArticle, costCalcRequest)
        }

        return findById(updatedArticle.id!!)
    }

    @Transactional
    override fun delete(id: Long) {
        if (!dependencies.articleRepository.existsById(id)) {
            throw ArticleNotFoundException("Article not found with id: $id")
        }
        dependencies.articleRepository.deleteById(id)
    }

    private fun createMugVariant(
        article: Article,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariant {
        val variant =
            MugArticleVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
            )

        return dependencies.articleMugVariantRepository.save(variant)
    }

    private fun createShirtVariant(
        article: Article,
        request: CreateShirtArticleVariantRequest,
    ): ShirtArticleVariant {
        val variant =
            ShirtArticleVariant(
                article = article,
                color = request.color,
                size = request.size,
                exampleImageFilename = request.exampleImageFilename,
            )

        return dependencies.articleShirtVariantRepository.save(variant)
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
        }
    }

    private fun buildArticleWithDetailsDto(article: Article): ArticleWithDetailsDto {
        val mugDetails =
            if (article.articleType == ArticleType.MUG) {
                dependencies.mugDetailsService.findByArticleId(article.id!!)
            } else {
                null
            }

        val shirtDetails =
            if (article.articleType == ArticleType.SHIRT) {
                dependencies.shirtDetailsService.findByArticleId(article.id!!)
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
            supplierId = article.supplierId,
            supplierName = article.supplierId?.let { dependencies.supplierService.getSupplierById(it).name },
            supplierArticleName = article.supplierArticleName,
            supplierArticleNumber = article.supplierArticleNumber,
            mugVariants =
                if (article.articleType ==
                    ArticleType.MUG
                ) {
                    article.mugVariants.map { dependencies.mugArticleVariantAssembler.toDto(it) }
                } else {
                    null
                },
            shirtVariants =
                if (article.articleType ==
                    ArticleType.SHIRT
                ) {
                    article.shirtVariants.map { dependencies.shirtArticleVariantAssembler.toDto(it) }
                } else {
                    null
                },
            mugDetails = mugDetails,
            shirtDetails = shirtDetails,
            costCalculation = article.costCalculation?.toDto(),
            createdAt = article.createdAt,
            updatedAt = article.updatedAt,
        )
    }

    private fun createCostCalculation(
        article: Article,
        request: CreateCostCalculationRequest,
    ) {
        // Validate VAT rates exist if provided
        request.purchaseVatRateId?.let {
            if (!dependencies.vatService.existsById(it)) {
                throw ArticleNotFoundException("Purchase VAT rate not found with id: $it")
            }
        }

        request.salesVatRateId?.let {
            if (!dependencies.vatService.existsById(it)) {
                throw ArticleNotFoundException("Sales VAT rate not found with id: $it")
            }
        }

        val costCalculation =
            CostCalculation(
                article = article,
                purchasePriceNet = request.purchasePriceNet,
                purchasePriceTax = request.purchasePriceTax,
                purchasePriceGross = request.purchasePriceGross,
                purchaseCostNet = request.purchaseCostNet,
                purchaseCostTax = request.purchaseCostTax,
                purchaseCostGross = request.purchaseCostGross,
                purchaseCostPercent = request.purchaseCostPercent,
                purchaseTotalNet = request.purchaseTotalNet,
                purchaseTotalTax = request.purchaseTotalTax,
                purchaseTotalGross = request.purchaseTotalGross,
                purchasePriceUnit = request.purchasePriceUnit,
                purchaseVatRateId = request.purchaseVatRateId,
                purchaseVatRatePercent = request.purchaseVatRatePercent,
                purchaseCalculationMode = request.purchaseCalculationMode,
                salesVatRateId = request.salesVatRateId,
                salesVatRatePercent = request.salesVatRatePercent,
                salesMarginNet = request.salesMarginNet,
                salesMarginTax = request.salesMarginTax,
                salesMarginGross = request.salesMarginGross,
                salesMarginPercent = request.salesMarginPercent,
                salesTotalNet = request.salesTotalNet,
                salesTotalTax = request.salesTotalTax,
                salesTotalGross = request.salesTotalGross,
                salesPriceUnit = request.salesPriceUnit,
                salesCalculationMode = request.salesCalculationMode,
                purchasePriceCorresponds = request.getPurchasePriceCorrespondsAsEnum(),
                salesPriceCorresponds = request.getSalesPriceCorrespondsAsEnum(),
                purchaseActiveRow = request.purchaseActiveRow,
                salesActiveRow = request.salesActiveRow,
            )

        dependencies.costCalculationRepository.save(costCalculation)
    }

    private fun updateCostCalculation(
        article: Article,
        request: UpdateCostCalculationRequest,
    ) {
        val costCalculation =
            dependencies.costCalculationRepository
                .findByArticleId(article.id!!)
                .orElse(null)

        if (costCalculation != null) {
            validateVatRatesForCostCalculation(request)
            updateCostCalculationProperties(costCalculation, request)
            dependencies.costCalculationRepository.save(costCalculation)
        } else {
            createCostCalculationFromUpdateRequest(article, request)
        }
    }

    private fun validateVatRatesForCostCalculation(request: UpdateCostCalculationRequest) {
        request.purchaseVatRateId?.let {
            if (!dependencies.vatService.existsById(it)) {
                throw ArticleNotFoundException("Purchase VAT rate not found with id: $it")
            }
        }

        request.salesVatRateId?.let {
            if (!dependencies.vatService.existsById(it)) {
                throw ArticleNotFoundException("Sales VAT rate not found with id: $it")
            }
        }
    }

    private fun updateCostCalculationProperties(
        costCalculation: CostCalculation,
        request: UpdateCostCalculationRequest,
    ) {
        costCalculation.purchasePriceNet = request.purchasePriceNet
        costCalculation.purchasePriceTax = request.purchasePriceTax
        costCalculation.purchasePriceGross = request.purchasePriceGross
        costCalculation.purchaseCostNet = request.purchaseCostNet
        costCalculation.purchaseCostTax = request.purchaseCostTax
        costCalculation.purchaseCostGross = request.purchaseCostGross
        costCalculation.purchaseCostPercent = request.purchaseCostPercent
        costCalculation.purchaseTotalNet = request.purchaseTotalNet
        costCalculation.purchaseTotalTax = request.purchaseTotalTax
        costCalculation.purchaseTotalGross = request.purchaseTotalGross
        costCalculation.purchasePriceUnit = request.purchasePriceUnit
        costCalculation.purchaseVatRateId = request.purchaseVatRateId
        costCalculation.purchaseVatRatePercent = request.purchaseVatRatePercent
        costCalculation.purchaseCalculationMode = request.purchaseCalculationMode
        costCalculation.salesVatRateId = request.salesVatRateId
        costCalculation.salesVatRatePercent = request.salesVatRatePercent
        costCalculation.salesMarginNet = request.salesMarginNet
        costCalculation.salesMarginTax = request.salesMarginTax
        costCalculation.salesMarginGross = request.salesMarginGross
        costCalculation.salesMarginPercent = request.salesMarginPercent
        costCalculation.salesTotalNet = request.salesTotalNet
        costCalculation.salesTotalTax = request.salesTotalTax
        costCalculation.salesTotalGross = request.salesTotalGross
        costCalculation.salesPriceUnit = request.salesPriceUnit
        costCalculation.salesCalculationMode = request.salesCalculationMode
        costCalculation.purchasePriceCorresponds = request.getPurchasePriceCorrespondsAsEnum()
        costCalculation.salesPriceCorresponds = request.getSalesPriceCorrespondsAsEnum()
        costCalculation.purchaseActiveRow = request.purchaseActiveRow
        costCalculation.salesActiveRow = request.salesActiveRow
    }

    private fun createCostCalculationFromUpdateRequest(
        article: Article,
        request: UpdateCostCalculationRequest,
    ) {
        createCostCalculation(
            article,
            CreateCostCalculationRequest(
                purchasePriceNet = request.purchasePriceNet,
                purchasePriceTax = request.purchasePriceTax,
                purchasePriceGross = request.purchasePriceGross,
                purchaseCostNet = request.purchaseCostNet,
                purchaseCostTax = request.purchaseCostTax,
                purchaseCostGross = request.purchaseCostGross,
                purchaseCostPercent = request.purchaseCostPercent,
                purchaseTotalNet = request.purchaseTotalNet,
                purchaseTotalTax = request.purchaseTotalTax,
                purchaseTotalGross = request.purchaseTotalGross,
                purchasePriceUnit = request.purchasePriceUnit,
                purchaseVatRateId = request.purchaseVatRateId,
                purchaseVatRatePercent = request.purchaseVatRatePercent,
                purchaseCalculationMode = request.purchaseCalculationMode,
                salesVatRateId = request.salesVatRateId,
                salesVatRatePercent = request.salesVatRatePercent,
                salesMarginNet = request.salesMarginNet,
                salesMarginTax = request.salesMarginTax,
                salesMarginGross = request.salesMarginGross,
                salesMarginPercent = request.salesMarginPercent,
                salesTotalNet = request.salesTotalNet,
                salesTotalTax = request.salesTotalTax,
                salesTotalGross = request.salesTotalGross,
                salesPriceUnit = request.salesPriceUnit,
                salesCalculationMode = request.salesCalculationMode,
                purchasePriceCorresponds = request.getPurchasePriceCorrespondsAsEnum(),
                salesPriceCorresponds = request.getSalesPriceCorrespondsAsEnum(),
                purchaseActiveRow = request.purchaseActiveRow,
                salesActiveRow = request.salesActiveRow,
            ),
        )
    }

    @Transactional(readOnly = true)
    override fun findPublicMugs(): List<PublicMugDto> {
        // Find all active mug articles with their details
        val mugs = dependencies.articleRepository.findAllActiveMugsWithDetails(ArticleType.MUG)

        return mugs.mapNotNull { article ->
            // Get mug details
            val mugDetails = dependencies.mugDetailsService.findByArticleId(article.id!!)

            // Filter only active variants
            val activeVariants = article.mugVariants.filter { it.active }

            // Get default variant for image from active variants only
            val defaultVariant = activeVariants.find { it.isDefault } ?: activeVariants.firstOrNull()

            // Convert price from cents to euros (assuming salesTotalGross is in cents)
            val price =
                article.costCalculation
                    ?.salesTotalGross
                    ?.toDouble()
                    ?.div(CENTS_TO_EUROS) ?: 0.0

            // Map active mug variants to public DTOs
            val publicVariants =
                activeVariants.map { variant ->
                    PublicMugVariantDto(
                        id = variant.id!!,
                        mugId = article.id,
                        colorCode = variant.outsideColorCode, // Using outside color as primary
                        name = variant.name,
                        exampleImageUrl =
                            variant.exampleImageFilename?.let { filename ->
                                dependencies.storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
                            },
                        articleVariantNumber = variant.articleVariantNumber,
                        isDefault = variant.isDefault,
                        active = variant.active,
                        exampleImageFilename = variant.exampleImageFilename,
                        createdAt = variant.createdAt,
                        updatedAt = variant.updatedAt,
                    )
                }

            // Only include if we have mug details
            mugDetails?.let {
                PublicMugDto(
                    id = article.id,
                    name = article.name,
                    price = price,
                    image =
                        defaultVariant?.exampleImageFilename?.let { filename ->
                            dependencies.storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
                        },
                    fillingQuantity = it.fillingQuantity,
                    descriptionShort = article.descriptionShort,
                    descriptionLong = article.descriptionLong,
                    heightMm = it.heightMm,
                    diameterMm = it.diameterMm,
                    printTemplateWidthMm = it.printTemplateWidthMm,
                    printTemplateHeightMm = it.printTemplateHeightMm,
                    dishwasherSafe = it.dishwasherSafe,
                    variants = publicVariants,
                )
            }
        }
    }

    override fun getArticlesByIds(ids: Collection<Long>): Map<Long, ArticleDto> {
        if (ids.isEmpty()) return emptyMap()
        val articles = dependencies.articleRepository.findAllById(ids)
        return articles.associate { a ->
            val dto =
                ArticleDto(
                    id = requireNotNull(a.id),
                    name = a.name,
                    descriptionShort = a.descriptionShort,
                    descriptionLong = a.descriptionLong,
                    active = a.active,
                    articleType = a.articleType,
                    categoryId = a.category.id!!,
                    categoryName = a.category.name,
                    subcategoryId = a.subcategory?.id,
                    subcategoryName = a.subcategory?.name,
                    supplierId = a.supplierId,
                    supplierName = a.supplierId?.let { dependencies.supplierService.getSupplierById(it).name },
                    supplierArticleName = a.supplierArticleName,
                    supplierArticleNumber = a.supplierArticleNumber,
                    mugVariants = null,
                    shirtVariants = null,
                    createdAt = a.createdAt,
                    updatedAt = a.updatedAt,
                )
            dto.id to dto
        }
    }

    override fun getMugVariantsByIds(ids: Collection<Long>): Map<Long, MugArticleVariantDto> {
        if (ids.isEmpty()) return emptyMap()
        val variants = dependencies.articleMugVariantRepository.findAllById(ids)
        return variants.associate { v ->
            val dto = dependencies.mugArticleVariantAssembler.toDto(v)
            dto.id to dto
        }
    }

    override fun getCurrentGrossPrice(articleId: Long): Long {
        val cost = dependencies.costCalculationRepository.findByArticleId(articleId).orElse(null)
        return cost?.salesTotalGross?.toLong() ?: 0L
    }

    override fun validateVariantBelongsToArticle(
        articleId: Long,
        variantId: Long,
    ): Boolean {
        val variantOpt = dependencies.articleMugVariantRepository.findById(variantId)
        return variantOpt.map { it.article.id == articleId }.orElse(false)
    }

    override fun getMugDetailsByArticleId(articleId: Long): MugArticleDetailsDto? = dependencies.mugDetailsService.findByArticleId(articleId)
}
