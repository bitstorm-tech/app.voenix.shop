package com.jotoai.voenix.shop.order.internal.repository

import com.jotoai.voenix.shop.order.internal.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, UUID> {
    /**
     * Finds all order items for a specific order
     */
    @Query(
        """
        SELECT oi FROM OrderItem oi
        LEFT JOIN FETCH oi.prompt p
        WHERE oi.order.id = :orderId
        ORDER BY oi.createdAt ASC
        """,
    )
    fun findByOrderId(
        @Param("orderId") orderId: UUID,
    ): List<OrderItem>

    /**
     * Finds order items by generated image ID
     */
    fun findByGeneratedImageId(generatedImageId: Long): List<OrderItem>
}
