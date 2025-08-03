package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.domain.orders.dto.CreateOrderRequest
import com.jotoai.voenix.shop.domain.orders.dto.OrderDto
import com.jotoai.voenix.shop.domain.orders.enums.OrderStatus
import com.jotoai.voenix.shop.domain.orders.service.OrderService
import com.jotoai.voenix.shop.domain.users.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
class OrderController(
    private val orderService: OrderService,
    private val userService: UserService,
) {
    /**
     * Creates an order from the user's current cart
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CreateOrderRequest,
    ): OrderDto {
        val userId = getCurrentUserId(userDetails)
        return orderService.createOrderFromCart(userId, request)
    }

    /**
     * Gets all orders for the current user
     */
    @GetMapping("/orders")
    fun getUserOrders(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) status: OrderStatus?,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto> {
        val userId = getCurrentUserId(userDetails)

        return if (status != null) {
            orderService.getUserOrdersByStatus(userId, status, pageable)
        } else {
            orderService.getUserOrders(userId, pageable)
        }
    }

    /**
     * Gets a specific order by ID
     */
    @GetMapping("/orders/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
    ): OrderDto {
        val userId = getCurrentUserId(userDetails)
        return orderService.getOrder(userId, orderId)
    }

    /**
     * Cancels an order (if allowed)
     */
    @PostMapping("/orders/{orderId}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
    ): OrderDto {
        val userId = getCurrentUserId(userDetails)
        return orderService.cancelOrder(userId, orderId)
    }

    private fun getCurrentUserId(userDetails: UserDetails): Long {
        val user = userService.getUserByEmail(userDetails.username)
        return user.id
    }
}
