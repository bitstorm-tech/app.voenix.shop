package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.assembler.ArticleAssembler
import com.jotoai.voenix.shop.domain.articles.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.domain.articles.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleSubCategoryRepository
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateCostCalculationRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.PublicMugDto
import com.jotoai.voenix.shop.domain.articles.dto.PublicMugVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateCostCalculationRequest
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.CostCalculation
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.CostCalculationRepository
import com.jotoai.voenix.shop.domain.articles.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.domain.articles.repository.ShirtArticleVariantRepository
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.StoragePathService
import com.jotoai.voenix.shop.domain.suppliers.repository.SupplierRepository
import com.jotoai.voenix.shop.domain.vat.repository.ValueAddedTaxRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val articleMugVariantRepository: MugArticleVariantRepository,
    private val articleShirtVariantRepository: ShirtArticleVariantRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
    private val articleSubCategoryRepository: ArticleSubCategoryRepository,
    private val supplierRepository: SupplierRepository,
    private val costCalculationRepository: CostCalculationRepository,
    private val valueAddedTaxRepository: ValueAddedTaxRepository,
    private val mugDetailsService: MugDetailsService,
    private val shirtDetailsService: ShirtDetailsService,
    private val articleAssembler: ArticleAssembler,
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
    private val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
    private val storagePathService: StoragePathService,
) {
    @Transactional(readOnly = true)
    fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType? = null,
        categoryId: Long? = null,
        subcategoryId: Long? = null,
        active: Boolean? = null,
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
            content = articlesPage.content.map { articleAssembler.toDto(it) },
            currentPage = articlesPage.number,
            totalPages = articlesPage.totalPages,
            totalElements = articlesPage.totalElements,
            size = articlesPage.size,
        )
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ArticleWithDetailsDto {
        // First, fetch article with basic details to determine the type
        val articleBasic =
            articleRepository.findByIdWithBasicDetails(id)
                ?: throw ResourceNotFoundException("Article not found with id: $id")

        // Then fetch with appropriate variants based on article type
        val article =
            when (articleBasic.articleType) {
                ArticleType.MUG -> articleRepository.findMugByIdWithDetails(id)
                ArticleType.SHIRT -> articleRepository.findShirtByIdWithDetails(id)
            } ?: throw ResourceNotFoundException("Article not found with id: $id")

        return buildArticleWithDetailsDto(article)
    }

    @Transactional
    fun create(request: CreateArticleRequest): ArticleWithDetailsDto {
        val category =
            articleCategoryRepository
                .findById(request.categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found with id: ${request.categoryId}") }
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
                supplierArticleName = request.supplierArticleName,
                supplierArticleNumber = request.supplierArticleNumber,
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
            this.supplierArticleName = request.supplierArticleName
            this.supplierArticleNumber = request.supplierArticleNumber
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
        }

        // Update cost calculation if provided
        request.costCalculation?.let { costCalcRequest ->
            updateCostCalculation(updatedArticle, costCalcRequest)
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
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariant {
        val variant =
            MugArticleVariant(
                article = article,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
            )

        return articleMugVariantRepository.save(variant)
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

        return articleShirtVariantRepository.save(variant)
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
            supplierArticleName = article.supplierArticleName,
            supplierArticleNumber = article.supplierArticleNumber,
            mugVariants =
                if (article.articleType ==
                    ArticleType.MUG
                ) {
                    article.mugVariants.map { mugArticleVariantAssembler.toDto(it) }
                } else {
                    null
                },
            shirtVariants =
                if (article.articleType ==
                    ArticleType.SHIRT
                ) {
                    article.shirtVariants.map { shirtArticleVariantAssembler.toDto(it) }
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
        val purchaseVatRate =
            request.purchaseVatRateId?.let {
                valueAddedTaxRepository
                    .findById(it)
                    .orElseThrow { ResourceNotFoundException("Purchase VAT rate not found with id: $it") }
            }

        val salesVatRate =
            request.salesVatRateId?.let {
                valueAddedTaxRepository
                    .findById(it)
                    .orElseThrow { ResourceNotFoundException("Sales VAT rate not found with id: $it") }
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
                purchaseVatRate = purchaseVatRate,
                purchaseVatRatePercent = request.purchaseVatRatePercent,
                purchaseCalculationMode = request.purchaseCalculationMode,
                salesVatRate = salesVatRate,
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

        costCalculationRepository.save(costCalculation)
    }

    private fun updateCostCalculation(
        article: Article,
        request: UpdateCostCalculationRequest,
    ) {
        val costCalculation =
            costCalculationRepository
                .findByArticleId(article.id!!)
                .orElse(null)

        if (costCalculation != null) {
            // Update existing cost calculation
            val purchaseVatRate =
                request.purchaseVatRateId?.let {
                    valueAddedTaxRepository
                        .findById(it)
                        .orElseThrow { ResourceNotFoundException("Purchase VAT rate not found with id: $it") }
                }

            val salesVatRate =
                request.salesVatRateId?.let {
                    valueAddedTaxRepository
                        .findById(it)
                        .orElseThrow { ResourceNotFoundException("Sales VAT rate not found with id: $it") }
                }

            // Update the existing cost calculation properties
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
            costCalculation.purchaseVatRate = purchaseVatRate
            costCalculation.purchaseVatRatePercent = request.purchaseVatRatePercent
            costCalculation.purchaseCalculationMode = request.purchaseCalculationMode
            costCalculation.salesVatRate = salesVatRate
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

            costCalculationRepository.save(costCalculation)
        } else {
            // Create new cost calculation if it doesn't exist
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
    }

    @Transactional(readOnly = true)
    fun findPublicMugs(): List<PublicMugDto> {
        // Find all active mug articles with their details
        val mugs = articleRepository.findAllActiveMugsWithDetails(ArticleType.MUG)

        return mugs.mapNotNull { article ->
            // Get mug details
            val mugDetails = mugDetailsService.findByArticleId(article.id!!)

            // Get default variant for image
            val defaultVariant = article.mugVariants.find { it.isDefault } ?: article.mugVariants.firstOrNull()

            // Convert price from cents to euros (assuming salesTotalGross is in cents)
            val price =
                article.costCalculation
                    ?.salesTotalGross
                    ?.toDouble()
                    ?.div(100) ?: 0.0

            // Map mug variants to public DTOs
            val publicVariants =
                article.mugVariants.map { variant ->
                    PublicMugVariantDto(
                        id = variant.id!!,
                        mugId = article.id,
                        colorCode = variant.outsideColorCode, // Using outside color as primary
                        exampleImageUrl =
                            variant.exampleImageFilename?.let { filename ->
                                storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
                            },
                        articleVariantNumber = variant.articleVariantNumber,
                        isDefault = variant.isDefault,
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
                            storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
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
}
