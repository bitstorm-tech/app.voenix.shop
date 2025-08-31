package com.jotoai.voenix.shop.cart.internal.assembler

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartItemDto
import com.jotoai.voenix.shop.cart.api.dto.CartSummaryDto
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.entity.CartItem
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import org.springframework.stereotype.Component

@Component
class CartAssembler(
    private val articleQueryService: ArticleQueryService,
    private val imageService: ImageService,
) {
    fun toDto(entity: Cart): CartDto =
        CartDto(
            id = requireNotNull(entity.id) { "Cart ID cannot be null when converting to DTO" },
            userId = entity.userId,
            status = entity.status,
            version = entity.version,
            expiresAt = entity.expiresAt,
            items =
                run {
                    val articleIds = entity.items.map { it.articleId }.distinct()
                    val variantIds = entity.items.map { it.variantId }.distinct()
                    val generatedImageIds = entity.items.mapNotNull { it.generatedImageId }.distinct()

                    val articlesById = articleQueryService.getArticlesByIds(articleIds)
                    val variantsById = articleQueryService.getMugVariantsByIds(variantIds)
                    val imagesById = imageService.find(generatedImageIds).mapValues { it.value as GeneratedImageDto }

                    entity.items.map { toItemDto(it, articlesById, variantsById, imagesById) }
                },
            totalItemCount = entity.getTotalItemCount(),
            totalPrice = entity.getTotalPrice(),
            isEmpty = entity.isEmpty(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toItemDto(
        entity: CartItem,
        articlesById: Map<Long, ArticleDto>,
        variantsById: Map<Long, MugArticleVariantDto>,
        imagesById: Map<Long, GeneratedImageDto>,
    ): CartItemDto {
        // Use FK fields directly
        val generatedImageId = entity.generatedImageId
        val generatedImageFilename: String? = generatedImageId?.let { imagesById[it]?.filename }
        val promptId = entity.promptId

        return CartItemDto(
            id = requireNotNull(entity.id) { "CartItem ID cannot be null when converting to DTO" },
            article =
                articlesById[entity.articleId]
                    ?: error("Missing ArticleDto for id: ${entity.articleId}"),
            variant =
                variantsById[entity.variantId]
                    ?: error("Missing MugArticleVariantDto for id: ${entity.variantId}"),
            quantity = entity.quantity,
            priceAtTime = entity.priceAtTime,
            originalPrice = entity.originalPrice,
            hasPriceChanged = entity.hasPriceChanged(),
            totalPrice = entity.getTotalPrice(),
            customData = entity.customData,
            generatedImageId = generatedImageId,
            generatedImageFilename = generatedImageFilename,
            promptId = promptId,
            position = entity.position,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
    }

    fun toSummaryDto(entity: Cart): CartSummaryDto =
        CartSummaryDto(
            itemCount = entity.getTotalItemCount(),
            totalPrice = entity.getTotalPrice(),
            hasItems = !entity.isEmpty(),
        )
}
