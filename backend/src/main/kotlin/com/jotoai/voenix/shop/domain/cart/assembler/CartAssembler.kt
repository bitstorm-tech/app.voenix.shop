package com.jotoai.voenix.shop.domain.cart.assembler

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.domain.cart.dto.CartDto
import com.jotoai.voenix.shop.domain.cart.dto.CartItemDto
import com.jotoai.voenix.shop.domain.cart.dto.CartSummaryDto
import com.jotoai.voenix.shop.domain.cart.entity.Cart
import com.jotoai.voenix.shop.domain.cart.entity.CartItem
import org.springframework.stereotype.Component

@Component
class CartAssembler(
    private val articleQueryService: ArticleQueryService,
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
                    val articleIds = entity.items.mapNotNull { it.article.id }.distinct()
                    val variantIds = entity.items.mapNotNull { it.variant.id }.distinct()
                    val articlesById = articleQueryService.getArticlesByIds(articleIds)
                    val variantsById = articleQueryService.getMugVariantsByIds(variantIds)
                    entity.items.map { toItemDto(it, articlesById, variantsById) }
                },
            totalItemCount = entity.getTotalItemCount(),
            totalPrice = entity.getTotalPrice(),
            isEmpty = entity.isEmpty(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toItemDto(
        entity: CartItem,
        articlesById: Map<Long, com.jotoai.voenix.shop.article.api.dto.ArticleDto>,
        variantsById: Map<Long, com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto>,
    ): CartItemDto {
        // Use FK fields directly
        val generatedImageId = entity.generatedImageId
        val generatedImageFilename: String? = null // TODO: Get from image service if needed
        val promptId = entity.prompt?.id

        return CartItemDto(
            id = requireNotNull(entity.id) { "CartItem ID cannot be null when converting to DTO" },
            article =
                articlesById[requireNotNull(entity.article.id)]
                    ?: throw IllegalStateException("Missing ArticleDto for id: ${entity.article.id}"),
            variant =
                variantsById[requireNotNull(entity.variant.id)]
                    ?: throw IllegalStateException("Missing MugArticleVariantDto for id: ${entity.variant.id}"),
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
