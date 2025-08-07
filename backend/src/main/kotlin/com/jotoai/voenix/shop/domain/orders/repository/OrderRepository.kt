package com.jotoai.voenix.shop.domain.orders.repository

import com.jotoai.voenix.shop.domain.orders.entity.Order
import com.jotoai.voenix.shop.domain.orders.enums.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    /**
     * Finds an order by order number
     */
    fun findByOrderNumber(orderNumber: String): Optional<Order>

    /**
     * Finds all orders for a user, ordered by creation date (newest first)
     */
    @Query(
        """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items oi
        LEFT JOIN FETCH oi.article a
        LEFT JOIN FETCH oi.variant v
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
        """,
    )
    fun findByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<Order>

    /**
     * Finds orders for a user with specific status
     */
    @Query(
        """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items oi
        LEFT JOIN FETCH oi.article a
        LEFT JOIN FETCH oi.variant v
        WHERE o.userId = :userId AND o.status = :status
        ORDER BY o.createdAt DESC
        """,
    )
    fun findByUserIdAndStatus(
        @Param("userId") userId: Long,
        @Param("status") status: OrderStatus,
        pageable: Pageable,
    ): Page<Order>

    /**
     * Finds an order by ID and user ID (for security)
     */
    @Query(
        """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items oi
        LEFT JOIN FETCH oi.article a
        LEFT JOIN FETCH oi.variant v
        LEFT JOIN FETCH oi.prompt p
        WHERE o.id = :orderId AND o.userId = :userId
        """,
    )
    fun findByIdAndUserId(
        @Param("orderId") orderId: UUID,
        @Param("userId") userId: Long,
    ): Optional<Order>

    /**
     * Finds an order by cart ID
     */
    fun findByCartId(cartId: Long): Optional<Order>

    /**
     * Checks if an order exists for the given cart
     */
    fun existsByCartId(cartId: Long): Boolean
}
