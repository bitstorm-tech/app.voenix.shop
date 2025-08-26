package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.cart.api.CartQueryService
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.CartOrderItemInfo
import com.jotoai.voenix.shop.cart.api.dto.CartSummaryDto
import com.jotoai.voenix.shop.cart.api.enums.CartStatus
import com.jotoai.voenix.shop.cart.api.exceptions.CartNotFoundException
import com.jotoai.voenix.shop.cart.internal.assembler.CartAssembler
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptNotFoundException
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class CartQueryServiceImpl(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val cartAssembler: CartAssembler,
    private val promptQueryService: PromptQueryService,
) : CartQueryService {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets or creates an active cart for the user
     */
    @Transactional
    override fun getOrCreateActiveCart(userId: Long): CartDto {
        // Validate user exists
        userService.getUserById(userId)

        val cart = getOrCreateActiveCartEntity(userId)
        return cartAssembler.toDto(cart)
    }

    /**
     * Internal method to get or create an active cart entity
     * Used by CartFacadeImpl to avoid circular dependencies
     */
    fun getOrCreateActiveCartEntity(userId: Long): Cart =
        cartRepository
            .findActiveCartByUserId(userId)
            .orElseGet {
                logger.debug { "Creating new cart for user: $userId" }
                createNewCart(userId)
            }

    /**
     * Gets a cart summary (item count and total price)
     */
    @Transactional(readOnly = true)
    override fun getCartSummary(userId: Long): CartSummaryDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElse(null)

        return if (cart != null) {
            cartAssembler.toSummaryDto(cart)
        } else {
            CartSummaryDto(itemCount = 0, totalPrice = 0L, hasItems = false)
        }
    }

    /**
     * Gets a cart by its ID
     */
    @Transactional(readOnly = true)
    override fun getCartById(id: Long): CartDto {
        val cart =
            cartRepository
                .findById(id)
                .orElseThrow { CartNotFoundException(userId = 0, isActiveCart = false) } // Generic not found
        return cartAssembler.toDto(cart)
    }

    /**
     * Finds an active cart for the specified user
     * Returns DTO or null if not found
     */
    @Transactional(readOnly = true)
    override fun findActiveCartByUserId(userId: Long): CartDto? {
        val cart = cartRepository.findActiveCartByUserId(userId).orElse(null)
        return cart?.let { cartAssembler.toDto(it) }
    }

    /**
     * Checks if an active cart exists for the user
     */
    @Transactional(readOnly = true)
    override fun existsActiveCartForUser(userId: Long): Boolean =
        cartRepository.findActiveCartByUserId(userId).isPresent

    /**
     * Gets active cart information for order creation.
     * Returns minimal cart data needed by the order module.
     */
    @Transactional(readOnly = true)
    override fun getActiveCartForOrder(userId: Long): CartOrderInfo? {
        val cart = cartRepository.findActiveCartByUserId(userId).orElse(null)
        return cart?.let {
            CartOrderInfo(
                id = it.id!!,
                userId = it.userId,
                status = it.status,
                items =
                    it.items.map { item ->
                        CartOrderItemInfo(
                            id = item.id!!,
                            articleId = item.articleId,
                            variantId = item.variantId,
                            quantity = item.quantity,
                            priceAtTime = item.priceAtTime,
                            totalPrice = item.getTotalPrice(),
                            generatedImageId = item.generatedImageId,
                            promptId = item.promptId,
                            promptText =
                                item.promptId?.let {
                                    try {
                                        promptQueryService.getPromptById(it).promptText
                                    } catch (e: PromptNotFoundException) {
                                        logger.warn(e) {
                                            "Failed to fetch prompt text for promptId=$it, prompt may have been deleted"
                                        }
                                        null // Handle case where prompt might have been deleted
                                    }
                                },
                            customData = item.customData,
                        )
                    },
                totalPrice = it.getTotalPrice(),
                isEmpty = it.isEmpty(),
            )
        }
    }

    /**
     * Marks a cart as converted after order creation.
     */
    @Transactional
    override fun markCartAsConverted(cartId: Long) {
        val cart =
            cartRepository
                .findById(cartId)
                .orElseThrow { CartNotFoundException(userId = 0, isActiveCart = false) }
        cart.status = CartStatus.CONVERTED
        cartRepository.save(cart)
        logger.info { "Marked cart $cartId as converted" }
    }

    private fun createNewCart(userId: Long): Cart {
        val cart =
            Cart(
                userId = userId,
                status = CartStatus.ACTIVE,
                expiresAt = OffsetDateTime.now().plusDays(DEFAULT_CART_EXPIRY_DAYS),
            )
        return cartRepository.save(cart)
    }

    companion object {
        private const val DEFAULT_CART_EXPIRY_DAYS = 30L
    }
}
