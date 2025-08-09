package com.jotoai.voenix.shop.cart.internal.repository

import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.api.enums.CartStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    /**
     * Finds the active cart for a user
     */
    @Query(
        """
        SELECT c FROM Cart c 
        LEFT JOIN FETCH c.items ci
        WHERE c.userId = :userId AND c.status = :status
        """,
    )
    fun findActiveCartByUserId(
        @Param("userId") userId: Long,
        @Param("status") status: CartStatus = CartStatus.ACTIVE,
    ): Optional<Cart>

    /**
     * Marks expired carts as abandoned
     */
    @Modifying
    @Query(
        """
        UPDATE Cart c 
        SET c.status = :newStatus, c.updatedAt = :now 
        WHERE c.status = :currentStatus 
        AND c.expiresAt IS NOT NULL 
        AND c.expiresAt < :now
        """,
    )
    fun markExpiredCartsAsAbandoned(
        @Param("currentStatus") currentStatus: CartStatus = CartStatus.ACTIVE,
        @Param("newStatus") newStatus: CartStatus = CartStatus.ABANDONED,
        @Param("now") now: OffsetDateTime,
    ): Int
}
