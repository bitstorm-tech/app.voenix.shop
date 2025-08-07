package com.jotoai.voenix.shop.domain.cart.assembler

import com.jotoai.voenix.shop.domain.articles.assembler.ArticleAssembler
import com.jotoai.voenix.shop.domain.articles.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.domain.cart.dto.CartDto
import com.jotoai.voenix.shop.domain.cart.dto.CartItemDto
import com.jotoai.voenix.shop.domain.cart.dto.CartSummaryDto
import com.jotoai.voenix.shop.domain.cart.entity.Cart
import com.jotoai.voenix.shop.domain.cart.entity.CartItem
import org.springframework.stereotype.Component

@Component
class CartAssembler(
    private val articleAssembler: ArticleAssembler,
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
) {
    fun toDto(entity: Cart): CartDto =
        CartDto(
            id = requireNotNull(entity.id) { "Cart ID cannot be null when converting to DTO" },
            userId = entity.userId,
            status = entity.status,
            version = entity.version,
            expiresAt = entity.expiresAt,
            items = entity.items.map { toItemDto(it) },
            totalItemCount = entity.getTotalItemCount(),
            totalPrice = entity.getTotalPrice(),
            isEmpty = entity.isEmpty(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toItemDto(entity: CartItem): CartItemDto {
        // Use FK fields directly
        val generatedImageId = entity.generatedImage?.id
        val generatedImageFilename = entity.generatedImage?.filename
        val promptId = entity.prompt?.id

        return CartItemDto(
            id = requireNotNull(entity.id) { "CartItem ID cannot be null when converting to DTO" },
            article = articleAssembler.toDto(entity.article),
            variant = mugArticleVariantAssembler.toDto(entity.variant),
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
