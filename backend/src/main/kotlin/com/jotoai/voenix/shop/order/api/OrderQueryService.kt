package com.jotoai.voenix.shop.order.api

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * Query service for Order module read operations.
 * This interface defines all read-only operations for order data.
 * It serves as the primary read API for other modules to access order information.
 */
interface OrderQueryService {
    /**
     * Gets an order by ID, ensuring it belongs to the user.
     */
    fun getOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto

    /**
     * Gets order data for PDF generation, ensuring it belongs to the user.
     * This method provides necessary data for external processing without exposing internal entities.
     */
    fun getOrderForPdf(
        userId: Long,
        orderId: UUID,
    ): OrderForPdfDto

    /**
     * Gets all orders for a user.
     */
    fun getUserOrders(
        userId: Long,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto>

    /**
     * Gets orders for a user with specific status.
     */
    fun getUserOrdersByStatus(
        userId: Long,
        status: OrderStatus,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto>
}
