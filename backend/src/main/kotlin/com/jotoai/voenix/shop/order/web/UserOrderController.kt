package com.jotoai.voenix.shop.order.web

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.order.api.OrderService
import com.jotoai.voenix.shop.order.api.dto.CreateOrderRequest
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.user.api.UserService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
class UserOrderController(
    private val orderService: OrderService,
    private val userService: UserService,
    private val pdfGenerationService: PdfGenerationService,
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

    /**
     * Downloads the PDF for a specific order
     */
    @GetMapping("/orders/{orderId}/pdf")
    fun downloadOrderPdf(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: UUID,
        response: HttpServletResponse,
    ) {
        val userId = getCurrentUserId(userDetails)

        // Get order data for PDF and validate ownership
        val orderForPdf = orderService.getOrderForPdf(userId, orderId)

        // Convert order to PDF data DTO using the public API
        val orderPdfData = pdfGenerationService.convertToOrderPdfData(orderForPdf)

        // Generate PDF
        val pdfBytes = pdfGenerationService.generateOrderPdf(orderPdfData)
        val filename = pdfGenerationService.getOrderPdfFilename(orderForPdf.orderNumber ?: "unknown")

        // Set response headers
        response.contentType = MediaType.APPLICATION_PDF_VALUE
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
        response.setHeader(HttpHeaders.CONTENT_LENGTH, pdfBytes.size.toString())
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
        response.setHeader(HttpHeaders.PRAGMA, "no-cache")
        response.setHeader(HttpHeaders.EXPIRES, "0")

        // Write PDF to response
        response.outputStream.use { outputStream ->
            outputStream.write(pdfBytes)
            outputStream.flush()
        }
    }

    private fun getCurrentUserId(userDetails: UserDetails): Long {
        val user = userService.getUserByEmail(userDetails.username)
        return user.id
    }
}