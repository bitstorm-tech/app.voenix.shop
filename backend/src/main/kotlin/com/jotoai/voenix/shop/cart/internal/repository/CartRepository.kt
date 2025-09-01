package com.jotoai.voenix.shop.cart.internal.repository

import com.jotoai.voenix.shop.cart.CartStatus
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    /**
     * Finds the active cart for a user
     */
    @Query(
        """
        SELECT DISTINCT c FROM Cart c 
        LEFT JOIN FETCH c.items ci
        WHERE c.userId = :userId AND c.status = :status
        """,
    )
    fun findActiveCartByUserId(
        @Param("userId") userId: Long,
        @Param("status") status: CartStatus = CartStatus.ACTIVE,
    ): Optional<Cart>
}
