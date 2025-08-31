package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.cart.api.CartQueryService
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.internal.assembler.OrderInfoAssembler
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartQueryServiceImpl(
    private val cartRepository: CartRepository,
    private val orderInfoAssembler: OrderInfoAssembler,
) : CartQueryService {

    /**
     * Gets active cart information for order creation.
     * Returns minimal cart data needed by the order module.
     */
    @Transactional(readOnly = true)
    override fun getActiveCartForOrder(userId: Long): CartOrderInfo? {
        val cart = cartRepository.findActiveCartByUserId(userId).orElse(null)
        return cart?.let { orderInfoAssembler.toOrderInfo(it) }
    }
}
