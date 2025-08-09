package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.order.api.OrderQueryService
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemForPdfDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import com.jotoai.voenix.shop.order.api.exception.OrderNotFoundException
import com.jotoai.voenix.shop.order.internal.entity.Order
import com.jotoai.voenix.shop.order.internal.repository.OrderRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class OrderQueryServiceImpl(
    private val orderRepository: OrderRepository,
    private val orderAssembler: OrderAssembler,
) : OrderQueryService {
    /**
     * Gets an order by ID, ensuring it belongs to the user
     */
    @Transactional(readOnly = true)
    override fun getOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto {
        val order = findOrderEntity(userId, orderId)
        return orderAssembler.toDto(order)
    }

    /**
     * Gets order data for PDF generation, ensuring it belongs to the user
     */
    @Transactional(readOnly = true)
    override fun getOrderForPdf(
        userId: Long,
        orderId: UUID,
    ): OrderForPdfDto {
        val order = findOrderEntity(userId, orderId)
        return OrderForPdfDto(
            id = order.id!!,
            orderNumber = order.orderNumber,
            userId = order.userId,
            items =
                order.items.map { orderItem ->
                    OrderItemForPdfDto(
                        id = orderItem.id!!,
                        quantity = orderItem.quantity,
                        generatedImageFilename = orderItem.generatedImageFilename,
                        articleId = orderItem.articleId,
                    )
                },
        )
    }

    /**
     * Internal helper method to find order entity by user and order ID
     */
    private fun findOrderEntity(
        userId: Long,
        orderId: UUID,
    ): Order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow { OrderNotFoundException("Order", "id", orderId) }

    /**
     * Gets all orders for a user
     */
    @Transactional(readOnly = true)
    override fun getUserOrders(
        userId: Long,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto> {
        val ordersPage = orderRepository.findByUserId(userId, pageable)
        return PaginatedResponse(
            content = ordersPage.content.map { orderAssembler.toDto(it) },
            currentPage = ordersPage.number,
            totalPages = ordersPage.totalPages,
            totalElements = ordersPage.totalElements,
            size = ordersPage.size,
        )
    }

    /**
     * Gets orders for a user with specific status
     */
    @Transactional(readOnly = true)
    override fun getUserOrdersByStatus(
        userId: Long,
        status: OrderStatus,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto> {
        val ordersPage = orderRepository.findByUserIdAndStatus(userId, status, pageable)
        return PaginatedResponse(
            content = ordersPage.content.map { orderAssembler.toDto(it) },
            currentPage = ordersPage.number,
            totalPages = ordersPage.totalPages,
            totalElements = ordersPage.totalElements,
            size = ordersPage.size,
        )
    }
}
