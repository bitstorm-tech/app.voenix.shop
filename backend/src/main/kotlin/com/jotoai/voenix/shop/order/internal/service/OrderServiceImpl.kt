package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.application.api.dto.PaginatedResponse
import com.jotoai.voenix.shop.application.api.exception.BadRequestException
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.cart.api.CartFacade
import com.jotoai.voenix.shop.cart.api.CartQueryService
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.GeneratedImageDto
import com.jotoai.voenix.shop.order.api.OrderService
import com.jotoai.voenix.shop.order.api.dto.CreateOrderRequest
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemDto
import com.jotoai.voenix.shop.order.api.dto.OrderItemForPdfDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import com.jotoai.voenix.shop.order.api.exception.OrderAlreadyExistsException
import com.jotoai.voenix.shop.order.api.exception.OrderNotFoundException
import com.jotoai.voenix.shop.order.internal.assembler.AddressAssembler
import com.jotoai.voenix.shop.order.internal.entity.Order
import com.jotoai.voenix.shop.order.internal.entity.OrderItem
import com.jotoai.voenix.shop.order.internal.repository.OrderRepository
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Suppress("LongParameterList", "TooManyFunctions")
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val cartQueryService: CartQueryService,
    private val cartFacade: CartFacade,
    private val userService: UserService,
    private val articleQueryService: ArticleQueryService,
    private val imageService: ImageService,
    private val entityManager: EntityManager,
    private val addressAssembler: AddressAssembler,
    @param:Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String,
) : OrderService {
    private val logger = KotlinLogging.logger {}

    /**
     * Creates an order from the user's active cart
     */
    @Transactional
    override fun createOrderFromCart(
        userId: Long,
        request: CreateOrderRequest,
    ): OrderDto {
        userService.getUserById(userId)
        val refreshedCart = validateAndRefreshCart(userId)
        val orderTotals = calculateOrderTotals(refreshedCart)

        val order = createOrderEntity(userId, request, refreshedCart, orderTotals)
        createOrderItems(refreshedCart, order)

        val savedOrder = saveAndRefreshOrder(order)
        cartQueryService.markCartAsConverted(refreshedCart.id)

        logOrderCreation(userId, refreshedCart, savedOrder, orderTotals.totalAmount)

        return toDto(savedOrder)
    }

    private fun validateAndRefreshCart(userId: Long): CartOrderInfo {
        val cartInfo =
            cartQueryService.getActiveCartForOrder(userId)
                ?: throw BadRequestException("No active cart found for user: $userId")

        validateCartForOrder(cartInfo)

        return cartFacade.refreshCartPricesForOrder(cartInfo.id)
    }

    private fun validateCartForOrder(cartInfo: CartOrderInfo) {
        if (cartInfo.isEmpty) {
            throw BadRequestException("Cannot create order from empty cart")
        }

        if (orderRepository.existsByCartId(cartInfo.id)) {
            throw OrderAlreadyExistsException(cartInfo.id)
        }
    }

    private fun calculateOrderTotals(cart: CartOrderInfo): OrderTotals {
        val subtotal = cart.totalPrice
        val taxAmount = calculateTax(subtotal)
        val shippingAmount = calculateShipping(cart)
        val totalAmount = subtotal + taxAmount + shippingAmount

        return OrderTotals(subtotal, taxAmount, shippingAmount, totalAmount)
    }

    private fun createOrderEntity(
        userId: Long,
        request: CreateOrderRequest,
        cart: CartOrderInfo,
        totals: OrderTotals,
    ): Order {
        val billingAddress =
            if (request.useShippingAsBilling || request.billingAddress == null) {
                addressAssembler.toEntity(request.shippingAddress)
            } else {
                addressAssembler.toEntity(request.billingAddress)
            }

        return Order(
            userId = userId,
            customerEmail = request.customerEmail,
            customerFirstName = request.customerFirstName,
            customerLastName = request.customerLastName,
            customerPhone = request.customerPhone,
            shippingAddress = addressAssembler.toEntity(request.shippingAddress),
            billingAddress = billingAddress,
            subtotal = totals.subtotal,
            taxAmount = totals.taxAmount,
            shippingAmount = totals.shippingAmount,
            totalAmount = totals.totalAmount,
            status = OrderStatus.PENDING,
            cartId = cart.id,
            notes = request.notes,
        )
    }

    private fun createOrderItems(
        cart: CartOrderInfo,
        order: Order,
    ) {
        cart.items.forEach { cartItem ->
            val orderItem =
                OrderItem(
                    order = order,
                    articleId = cartItem.articleId,
                    variantId = cartItem.variantId,
                    quantity = cartItem.quantity,
                    pricePerItem = cartItem.priceAtTime,
                    totalPrice = cartItem.totalPrice,
                    generatedImageId = cartItem.generatedImageId,
                    promptId = cartItem.promptId,
                    customData = cartItem.customData ?: emptyMap(),
                )
            order.addItem(orderItem)
        }
    }

    private fun saveAndRefreshOrder(order: Order): Order {
        val savedOrder = orderRepository.save(order)
        entityManager.flush()
        entityManager.refresh(savedOrder)
        return savedOrder
    }

    private fun logOrderCreation(
        userId: Long,
        cart: CartOrderInfo,
        order: Order,
        totalAmount: Long,
    ) {
        logger.info {
            "Created order ${order.orderNumber} for user $userId from cart ${cart.id} with total amount $totalAmount"
        }
    }

    private data class OrderTotals(
        val subtotal: Long,
        val taxAmount: Long,
        val shippingAmount: Long,
        val totalAmount: Long,
    )

    /**
     * Gets an order by ID, ensuring it belongs to the user
     */
    @Transactional(readOnly = true)
    override fun getOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto {
        val order = findOrderEntity(userId, orderId)
        return toDto(order)
    }

    /**
     * Gets order data for PDF generation, ensuring it belongs to the user
     */
    @Transactional(readOnly = true)
    override fun getOrderForPdf(
        userId: Long,
        orderId: UUID,
    ): OrderForPdfDto {
        val order = findOrderEntity(userId, orderId)

        // Fetch image filenames for all items with generated images
        val generatedImageIds = order.items.mapNotNull { it.generatedImageId }.distinct()
        val imagesById = imageService.find(generatedImageIds).mapValues { it.value as GeneratedImageDto }

        return OrderForPdfDto(
            id = order.id!!,
            orderNumber = order.orderNumber,
            userId = order.userId,
            items =
                order.items.map { orderItem ->
                    OrderItemForPdfDto(
                        id = orderItem.id!!,
                        quantity = orderItem.quantity,
                        generatedImageId = orderItem.generatedImageId,
                        generatedImageFilename = orderItem.generatedImageId?.let { imagesById[it]?.filename },
                        articleId = orderItem.articleId,
                        variantId = orderItem.variantId,
                    )
                },
        )
    }

    /**
     * Gets all orders for a user
     */
    @Transactional(readOnly = true)
    override fun getUserOrders(
        userId: Long,
        pageable: Pageable,
    ): PaginatedResponse<OrderDto> {
        val ordersPage = orderRepository.findByUserId(userId, pageable)
        return PaginatedResponse(
            content = ordersPage.content.map { toDto(it) },
            totalElements = ordersPage.totalElements,
            totalPages = ordersPage.totalPages,
            size = ordersPage.size,
            number = ordersPage.number,
            first = ordersPage.isFirst,
            last = ordersPage.isLast,
            numberOfElements = ordersPage.numberOfElements,
            empty = ordersPage.isEmpty,
        )
    }

    private fun findOrderEntity(
        userId: Long,
        orderId: UUID,
    ): Order =
        orderRepository
            .findByIdAndUserId(orderId, userId)
            .orElseThrow { OrderNotFoundException("Order", "id", orderId) }

    private fun toDto(entity: Order): OrderDto =
        OrderDto(
            id = requireNotNull(entity.id) { "Order ID cannot be null when converting to DTO" },
            orderNumber = requireNotNull(entity.orderNumber) { "Order number should be generated by database" },
            customerEmail = entity.customerEmail,
            customerFirstName = entity.customerFirstName,
            customerLastName = entity.customerLastName,
            customerPhone = entity.customerPhone,
            shippingAddress = addressAssembler.toDto(entity.shippingAddress),
            billingAddress = entity.billingAddress?.let { addressAssembler.toDto(it) },
            subtotal = entity.subtotal,
            taxAmount = entity.taxAmount,
            shippingAmount = entity.shippingAmount,
            totalAmount = entity.totalAmount,
            status = entity.status,
            cartId = entity.cartId,
            notes = entity.notes,
            items =
                run {
                    val articleIds = entity.items.map { it.articleId }.distinct()
                    val variantIds = entity.items.map { it.variantId }.distinct()
                    val generatedImageIds = entity.items.mapNotNull { it.generatedImageId }.distinct()

                    val articlesById = articleQueryService.getArticlesByIds(articleIds)
                    val variantsById = articleQueryService.getMugVariantsByIds(variantIds)
                    val imagesById = imageService.find(generatedImageIds).mapValues { it.value as GeneratedImageDto }

                    entity.items.map { toItemDto(it, articlesById, variantsById, imagesById) }
                },
            pdfUrl = generatePdfUrl(entity.id),
            createdAt = requireNotNull(entity.createdAt) { "Order createdAt cannot be null when converting to DTO" },
            updatedAt = requireNotNull(entity.updatedAt) { "Order updatedAt cannot be null when converting to DTO" },
        )

    private fun toItemDto(
        entity: OrderItem,
        articlesById: Map<Long, ArticleDto>,
        variantsById: Map<Long, MugArticleVariantDto>,
        imagesById: Map<Long, GeneratedImageDto>,
    ): OrderItemDto =
        OrderItemDto(
            id = requireNotNull(entity.id) { "OrderItem ID cannot be null when converting to DTO" },
            article =
                articlesById[entity.articleId]
                    ?: error("Missing ArticleDto for id: ${entity.articleId}"),
            variant =
                variantsById[entity.variantId]
                    ?: error("Missing MugArticleVariantDto for id: ${entity.variantId}"),
            quantity = entity.quantity,
            pricePerItem = entity.pricePerItem,
            totalPrice = entity.totalPrice,
            generatedImageId = entity.generatedImageId,
            generatedImageFilename = entity.generatedImageId?.let { imagesById[it]?.filename },
            promptId = entity.promptId,
            customData = entity.customData,
            createdAt =
                requireNotNull(entity.createdAt) {
                    "OrderItem createdAt cannot be null when converting to DTO"
                },
        )

    private fun generatePdfUrl(orderId: UUID): String = "$appBaseUrl/api/user/orders/$orderId/pdf"

    /**
     * Calculates tax amount (8% for now, configurable later)
     */
    private fun calculateTax(subtotal: Long): Long = (subtotal * TAX_RATE).toLong()

    /**
     * Calculates shipping amount ($4.99 flat rate for now)
     */
    private fun calculateShipping(cart: CartOrderInfo): Long = if (cart.isEmpty) 0L else SHIPPING_RATE_CENTS

    companion object {
        private const val TAX_RATE = 0.08 // 8%
        private const val SHIPPING_RATE_CENTS = 499L // $4.99
    }
}
