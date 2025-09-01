package com.jotoai.voenix.shop.cart.internal.assembler

import com.jotoai.voenix.shop.cart.CartOrderInfo
import com.jotoai.voenix.shop.cart.CartOrderItemInfo
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class OrderInfoAssembler(
    private val promptQueryService: PromptQueryService,
) {
    private val logger = KotlinLogging.logger {}

    fun toOrderInfo(cart: Cart): CartOrderInfo =
        CartOrderInfo(
            id = requireNotNull(cart.id) { "Cart ID cannot be null for order info" },
            userId = cart.userId,
            status = cart.status,
            items =
                cart.items.map { item ->
                    CartOrderItemInfo(
                        id = requireNotNull(item.id) { "CartItem ID cannot be null for order item info" },
                        articleId = item.articleId,
                        variantId = item.variantId,
                        quantity = item.quantity,
                        priceAtTime = item.priceAtTime,
                        totalPrice = item.getTotalPrice(),
                        generatedImageId = item.generatedImageId,
                        promptId = item.promptId,
                        promptText =
                            item.promptId?.let { pid ->
                                try {
                                    promptQueryService.getPromptById(pid).promptText
                                } catch (e: PromptNotFoundException) {
                                    logger.warn(e) {
                                        "Failed to fetch prompt text for promptId=$pid, prompt may have been deleted"
                                    }
                                    null
                                }
                            },
                        customData = item.customData,
                    )
                },
            totalPrice = cart.getTotalPrice(),
            isEmpty = cart.isEmpty(),
        )
}
