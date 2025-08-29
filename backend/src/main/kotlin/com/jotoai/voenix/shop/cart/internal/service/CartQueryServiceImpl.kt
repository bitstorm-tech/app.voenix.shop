package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.cart.api.CartQueryService
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.CartSummaryDto
import com.jotoai.voenix.shop.cart.api.enums.CartStatus
import com.jotoai.voenix.shop.cart.internal.assembler.CartAssembler
import com.jotoai.voenix.shop.cart.internal.assembler.OrderInfoAssembler
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartQueryServiceImpl(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val cartAssembler: CartAssembler,
    private val orderInfoAssembler: OrderInfoAssembler,
    private val cartInternalService: CartInternalService,
) : CartQueryService {
    private val logger = KotlinLogging.logger {}

    /**
     * Gets or creates an active cart for the user
     */
    @Transactional
    override fun getOrCreateActiveCart(userId: Long): CartDto {
        // Validate user exists
        userService.getUserById(userId)

        val cart = cartInternalService.getOrCreateActiveCartEntity(userId)
        return cartAssembler.toDto(cart)
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
     * Gets active cart information for order creation.
     * Returns minimal cart data needed by the order module.
     */
    @Transactional(readOnly = true)
    override fun getActiveCartForOrder(userId: Long): CartOrderInfo? {
        val cart = cartRepository.findActiveCartByUserId(userId).orElse(null)
        return cart?.let { orderInfoAssembler.toOrderInfo(it) }
    }

    /**
     * Marks a cart as converted after order creation.
     */
    @Transactional
    override fun markCartAsConverted(cartId: Long) {
        val cart =
            cartRepository
                .findById(cartId)
                .orElseThrow { ResourceNotFoundException("Cart not found with id: $cartId") }
        cart.status = CartStatus.CONVERTED
        cartRepository.save(cart)
        logger.info { "Marked cart $cartId as converted" }
    }
}
