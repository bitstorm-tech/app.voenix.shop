package com.jotoai.voenix.shop.order

import com.jotoai.voenix.shop.application.PaginatedResponse
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * Primary service interface for Order module operations.
 * This interface consolidates all order-related functionality including:
 * - User-facing command operations (create, cancel)
 * - Administrative operations (update status)
 * - Query operations (retrieve orders, PDF data)
 *
 * This simplified design replaces the previous CQRS pattern with a single,
 * cohesive service interface that maintains clear business boundaries.
 */
interface OrderService {
    /**
     * Creates an order from the user's active cart.
     *
     * @param userId The ID of the user creating the order
     * @param request The order creation request containing customer and address information
     * @return The created order DTO
     * @throws BadRequestException if no active cart found or cart is empty or order already exists for the cart
     */
    fun createOrderFromCart(
        userId: Long,
        request: CreateOrderRequest,
    ): OrderDto

    /**
     * Gets an order by ID, ensuring it belongs to the user.
     * This method enforces user ownership and only returns orders that belong to the requesting user.
     *
     * @param userId The ID of the user requesting the order
     * @param orderId The ID of the order to retrieve
     * @return The order DTO
     * @throws BadRequestException if order not found or doesn't belong to the user
     */
    fun getOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto

    /**
     * Gets all orders for a user with pagination support.
     * Returns orders sorted by creation date in descending order (newest first).
     */
    fun getUserOrders(
        userId: Long,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto>
}
