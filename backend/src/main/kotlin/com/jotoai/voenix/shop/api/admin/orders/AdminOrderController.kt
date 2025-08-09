package com.jotoai.voenix.shop.api.admin.orders

import com.jotoai.voenix.shop.order.api.AdminOrderFacade
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * REST controller for administrative order operations.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
class AdminOrderController(
    private val adminOrderFacade: AdminOrderFacade,
) {
    /**
     * Updates the status of an order.
     * This is an administrative operation that bypasses user ownership checks.
     */
    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: UUID,
        @RequestBody request: UpdateOrderStatusRequest,
    ): OrderDto = adminOrderFacade.updateOrderStatus(orderId, request.status)
}

/**
 * Request DTO for updating order status.
 */
data class UpdateOrderStatusRequest(
    val status: OrderStatus,
)
