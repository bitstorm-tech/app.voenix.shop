package com.jotoai.voenix.shop.article.internal.config

import com.jotoai.voenix.shop.article.internal.assembler.ArticleAssembler
import com.jotoai.voenix.shop.article.internal.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.article.internal.categories.repository.ArticleSubCategoryRepository
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.CostCalculationRepository
import com.jotoai.voenix.shop.article.internal.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.article.internal.repository.ShirtArticleVariantRepository
import com.jotoai.voenix.shop.article.internal.service.MugDetailsService
import com.jotoai.voenix.shop.article.internal.service.ShirtDetailsService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.supplier.api.SupplierService
import com.jotoai.voenix.shop.vat.api.VatService
import org.springframework.stereotype.Component

@Component
data class ArticleServiceDependencies(
    // Repository dependencies
    val articleRepository: ArticleRepository,
    val articleMugVariantRepository: MugArticleVariantRepository,
    val articleShirtVariantRepository: ShirtArticleVariantRepository,
    val articleCategoryRepository: ArticleCategoryRepository,
    val articleSubCategoryRepository: ArticleSubCategoryRepository,
    val costCalculationRepository: CostCalculationRepository,
    
    // Service dependencies
    val supplierService: SupplierService,
    val vatService: VatService,
    val mugDetailsService: MugDetailsService,
    val shirtDetailsService: ShirtDetailsService,
    val storagePathService: StoragePathService,
    
    // Assembler dependencies
    val articleAssembler: ArticleAssembler,
    val mugArticleVariantAssembler: MugArticleVariantAssembler,
    val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
)
