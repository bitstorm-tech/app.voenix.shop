package com.jotoai.voenix.shop.order.api

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.order.api.dto.CreateOrderRequest
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
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
    // Command Operations (User-facing)

    /**
     * Creates an order from the user's active cart.
     *
     * @param userId The ID of the user creating the order
     * @param request The order creation request containing customer and address information
     * @return The created order DTO
     * @throws BadRequestException if no active cart found or cart is empty
     * @throws OrderAlreadyExistsException if an order already exists for the cart
     */
    fun createOrderFromCart(
        userId: Long,
        request: CreateOrderRequest,
    ): OrderDto

    /**
     * Cancels an order (if allowed).
     * Only the order owner can cancel their own orders, and only if the order status permits cancellation.
     *
     * @param userId The ID of the user attempting to cancel the order
     * @param orderId The ID of the order to cancel
     * @return The updated order DTO
     * @throws OrderNotFoundException if order not found or doesn't belong to the user
     * @throws OrderCannotBeCancelledException if the order cannot be cancelled due to its current status
     */
    fun cancelOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto

    // Administrative Operations

    /**
     * Updates an order status (administrative operation).
     * This operation should only be accessible by administrators and bypasses user ownership checks.
     *
     * @param orderId The ID of the order to update
     * @param status The new status to set
     * @return The updated order DTO
     * @throws OrderNotFoundException if order not found
     */
    fun updateOrderStatus(
        orderId: UUID,
        status: OrderStatus,
    ): OrderDto

    // Query Operations

    /**
     * Gets an order by ID, ensuring it belongs to the user.
     * This method enforces user ownership and only returns orders that belong to the requesting user.
     *
     * @param userId The ID of the user requesting the order
     * @param orderId The ID of the order to retrieve
     * @return The order DTO
     * @throws OrderNotFoundException if order not found or doesn't belong to the user
     */
    fun getOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto

    /**
     * Gets order data for PDF generation, ensuring it belongs to the user.
     * This method provides only the necessary data for external PDF processing
     * without exposing internal entities or sensitive information.
     *
     * @param userId The ID of the user requesting the order data
     * @param orderId The ID of the order to retrieve for PDF generation
     * @return The order data suitable for PDF generation
     * @throws OrderNotFoundException if order not found or doesn't belong to the user
     */
    fun getOrderForPdf(
        userId: Long,
        orderId: UUID,
    ): OrderForPdfDto

    /**
     * Gets all orders for a user with pagination support.
     * Returns orders sorted by creation date in descending order (newest first).
     *
     * @param userId The ID of the user whose orders to retrieve
     * @param pageable Pagination parameters
     * @return Paginated response containing the user's orders
     */
    fun getUserOrders(
        userId: Long,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto>

    /**
     * Gets orders for a user with specific status and pagination support.
     * Returns orders filtered by the specified status, sorted by creation date in descending order.
     *
     * @param userId The ID of the user whose orders to retrieve
     * @param status The order status to filter by
     * @param pageable Pagination parameters
     * @return Paginated response containing the user's orders with the specified status
     */
    fun getUserOrdersByStatus(
        userId: Long,
        status: OrderStatus,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto>
}
