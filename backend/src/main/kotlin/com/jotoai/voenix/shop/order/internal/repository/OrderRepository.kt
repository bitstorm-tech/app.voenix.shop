package com.jotoai.voenix.shop.order.internal.repository

import com.jotoai.voenix.shop.order.internal.entity.Order
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    /**
     * Finds all orders for a user, ordered by creation date (newest first)
     */
    @Query(
        """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items oi
        WHERE o.userId = :userId
        ORDER BY o.createdAt DESC
        """,
    )
    fun findByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<Order>

    /**
     * Finds an order by ID and user ID (for security)
     */
    @Query(
        """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items oi
        WHERE o.id = :orderId AND o.userId = :userId
        """,
    )
    fun findByIdAndUserId(
        @Param("orderId") orderId: UUID,
        @Param("userId") userId: Long,
    ): Optional<Order>

    /**
     * Checks if an order exists for the given cart
     */
    fun existsByCartId(cartId: Long): Boolean
}
