package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.cart.api.enums.CartStatus
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Internal service for cart entity operations.
 * This service provides shared functionality for working with cart entities
 * within the cart module. It should only be used by other services within the module.
 */
@Service
class CartInternalService(
    private val cartRepository: CartRepository,
) {
    private val logger = LoggerFactory.getLogger(CartInternalService::class.java)

    /**
     * Gets or creates an active cart entity for the user.
     */
    @Transactional
    fun getOrCreateActiveCartEntity(userId: Long): Cart =
        cartRepository
            .findActiveCartByUserId(userId)
            .orElseGet {
                logger.debug("Creating new cart for user: {}", userId)
                createNewCart(userId)
            }

    /**
     * Creates a new cart for the user.
     */
    private fun createNewCart(userId: Long): Cart {
        val cart =
            Cart(
                userId = userId,
                status = CartStatus.ACTIVE,
                expiresAt = OffsetDateTime.now().plusDays(DEFAULT_CART_EXPIRY_DAYS),
            )
        return cartRepository.save(cart)
    }

    /**
     * Saves a cart entity.
     */
    fun saveCart(cart: Cart): Cart = cartRepository.save(cart)

    /**
     * Finds an active cart entity by user ID.
     */
    fun findActiveCartEntityByUserId(userId: Long): Cart? = cartRepository.findActiveCartByUserId(userId).orElse(null)

    companion object {
        private const val DEFAULT_CART_EXPIRY_DAYS = 30L
    }
}
