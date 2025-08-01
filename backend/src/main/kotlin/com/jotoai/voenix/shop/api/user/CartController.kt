package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.domain.cart.dto.AddToCartRequest
import com.jotoai.voenix.shop.domain.cart.dto.CartDto
import com.jotoai.voenix.shop.domain.cart.dto.CartSummaryDto
import com.jotoai.voenix.shop.domain.cart.dto.UpdateCartItemRequest
import com.jotoai.voenix.shop.domain.cart.service.CartService
import com.jotoai.voenix.shop.domain.users.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/cart")
@PreAuthorize("hasRole('USER')")
class CartController(
    private val cartService: CartService,
    private val userService: UserService,
) {
    /**
     * Gets the current user's active cart
     */
    @GetMapping
    fun getCart(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): CartDto {
        val userId = getCurrentUserId(userDetails)
        return cartService.getOrCreateActiveCart(userId)
    }

    /**
     * Gets a summary of the current user's cart (item count and total price)
     */
    @GetMapping("/summary")
    fun getCartSummary(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): CartSummaryDto {
        val userId = getCurrentUserId(userDetails)
        return cartService.getCartSummary(userId)
    }

    /**
     * Adds an item to the cart
     */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun addToCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: AddToCartRequest,
    ): CartDto {
        val userId = getCurrentUserId(userDetails)
        return cartService.addToCart(userId, request)
    }

    /**
     * Updates a cart item (quantity or custom data)
     */
    @PutMapping("/items/{itemId}")
    fun updateCartItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long,
        @Valid @RequestBody request: UpdateCartItemRequest,
    ): CartDto {
        val userId = getCurrentUserId(userDetails)
        return cartService.updateCartItem(userId, itemId, request)
    }

    /**
     * Removes an item from the cart
     */
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeFromCart(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable itemId: Long,
    ) {
        val userId = getCurrentUserId(userDetails)
        cartService.removeFromCart(userId, itemId)
    }

    /**
     * Clears all items from the cart
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearCart(
        @AuthenticationPrincipal userDetails: UserDetails,
    ) {
        val userId = getCurrentUserId(userDetails)
        cartService.clearCart(userId)
    }

    /**
     * Refreshes cart prices to current product prices
     */
    @PostMapping("/refresh-prices")
    fun refreshCartPrices(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): CartDto {
        val userId = getCurrentUserId(userDetails)
        return cartService.refreshCartPrices(userId)
    }

    private fun getCurrentUserId(userDetails: UserDetails): Long {
        val user = userService.getUserByEmail(userDetails.username)
        return user.id
    }
}
