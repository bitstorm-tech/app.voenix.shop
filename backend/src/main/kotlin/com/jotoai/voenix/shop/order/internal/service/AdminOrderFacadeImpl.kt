package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.order.api.AdminOrderFacade
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import com.jotoai.voenix.shop.order.api.exception.OrderNotFoundException
import com.jotoai.voenix.shop.order.internal.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdminOrderFacadeImpl(
    private val orderRepository: OrderRepository,
    private val orderAssembler: OrderAssembler,
) : AdminOrderFacade {
    private val logger = LoggerFactory.getLogger(AdminOrderFacadeImpl::class.java)

    /**
     * Updates an order status (administrative operation)
     */
    @Transactional
    override fun updateOrderStatus(
        orderId: UUID,
        status: OrderStatus,
    ): OrderDto {
        val order =
            orderRepository
                .findById(orderId)
                .orElseThrow { OrderNotFoundException("Order", "id", orderId) }

        order.status = status
        val savedOrder = orderRepository.save(order)

        logger.info("Admin updated order {} status to {}", orderId, status)

        return orderAssembler.toDto(savedOrder)
    }
}
