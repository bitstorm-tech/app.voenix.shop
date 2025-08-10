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
import com.jotoai.voenix.shop.user.api.UserQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class CartQueryServiceImpl(
    private val cartRepository: CartRepository,
    private val userQueryService: UserQueryService,
    private val cartAssembler: CartAssembler,
) : CartQueryService {
    private val logger = LoggerFactory.getLogger(CartQueryServiceImpl::class.java)

    /**
     * Gets or creates an active cart for the user
     */
    @Transactional
    override fun getOrCreateActiveCart(userId: Long): CartDto {
        // Validate user exists
        userQueryService.getUserById(userId)

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
                logger.debug("Creating new cart for user: {}", userId)
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
                            promptText = item.prompt,
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
        logger.info("Marked cart {} as converted", cartId)
    }

    /**
     * Internal method to get the cart entity for order service
     * This method is used internally by the order module
     */
    fun findActiveCartEntityByUserId(userId: Long): Cart? = cartRepository.findActiveCartByUserId(userId).orElse(null)

    /**
     * Internal method to find active cart entity by user ID and throw exception if not found
     * This method is used internally by the order module
     */
    fun getActiveCartEntityByUserId(userId: Long): Cart =
        cartRepository
            .findActiveCartByUserId(userId)
            .orElseThrow {
                com.jotoai.voenix.shop.common.exception
                    .BadRequestException("No active cart found for user: $userId")
            }

    /**
     * Internal method to save a cart entity
     * Used by other modules that need to persist cart changes
     */
    fun saveCartEntity(cart: Cart): Cart = cartRepository.save(cart)

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
