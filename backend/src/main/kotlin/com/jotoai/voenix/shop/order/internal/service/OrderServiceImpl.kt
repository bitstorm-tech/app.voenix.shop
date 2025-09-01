package com.jotoai.voenix.shop.order.internal.service

import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.application.PaginatedResponse
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.cart.CartOrderInfo
import com.jotoai.voenix.shop.cart.CartService
import com.jotoai.voenix.shop.image.GeneratedImageDto
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.order.AddressDto
import com.jotoai.voenix.shop.order.CreateOrderRequest
import com.jotoai.voenix.shop.order.OrderDto
import com.jotoai.voenix.shop.order.OrderItemDto
import com.jotoai.voenix.shop.order.OrderService
import com.jotoai.voenix.shop.order.OrderStatus
import com.jotoai.voenix.shop.order.internal.dto.OrderForPdfDto
import com.jotoai.voenix.shop.order.internal.entity.Address
import com.jotoai.voenix.shop.order.internal.entity.Order
import com.jotoai.voenix.shop.order.internal.entity.OrderItem
import com.jotoai.voenix.shop.order.internal.repository.OrderRepository
import com.jotoai.voenix.shop.user.UserService
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
    private val cartService: CartService,
    private val userService: UserService,
    private val articleQueryService: ArticleQueryService,
    private val imageService: ImageService,
    private val entityManager: EntityManager,
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
        cartService.markCartAsConverted(refreshedCart.id)

        logOrderCreation(userId, refreshedCart, savedOrder, orderTotals.totalAmount)

        return toDto(savedOrder)
    }

    private fun validateAndRefreshCart(userId: Long): CartOrderInfo {
        val cartInfo =
            cartService.getActiveCartForOrder(userId)
                ?: throw BadRequestException("No active cart found for user: $userId")

        validateCartForOrder(cartInfo)

        return cartService.refreshCartPricesForOrder(cartInfo.id)
    }

    private fun validateCartForOrder(cartInfo: CartOrderInfo) {
        if (cartInfo.isEmpty) {
            throw BadRequestException("Cannot create order from empty cart")
        }

        if (orderRepository.existsByCartId(cartInfo.id)) {
            throw BadRequestException("Order already exists for cart: ${cartInfo.id}")
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
        val billingAddress = (request.billingAddress ?: request.shippingAddress).toEntity()

        return Order(
            userId = userId,
            customerEmail = request.customerEmail,
            customerFirstName = request.customerFirstName,
            customerLastName = request.customerLastName,
            customerPhone = request.customerPhone,
            shippingAddress = request.shippingAddress.toEntity(),
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
    internal fun getOrderForPdf(
        userId: Long,
        orderId: UUID,
    ): OrderForPdfDto {
        val order = findOrderEntity(userId, orderId)

        // Fetch image filenames for all items with generated images
        val generatedImageIds = order.items.mapNotNull { it.generatedImageId }.distinct()
        val imagesById = imageService.find(generatedImageIds).mapValues { it.value as GeneratedImageDto }

        return OrderForPdfDto(
            id = order.id!!,
            orderNumber = order.orderNumber ?: "UNKNOWN",
            userId = order.userId,
            items =
                order.items.map { orderItem ->
                    OrderForPdfDto.PdfItem(
                        quantity = orderItem.quantity,
                        imageFilename = orderItem.generatedImageId?.let { imagesById[it]?.filename },
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
            .orElseThrow { BadRequestException("Order not found with id: $orderId") }

    private fun toDto(entity: Order): OrderDto {
        val itemDtos = convertOrderItems(entity.items)

        return OrderDto(
            id = requireNotNull(entity.id) { "Order ID cannot be null when converting to DTO" },
            orderNumber = requireNotNull(entity.orderNumber) { "Order number should be generated by database" },
            customerEmail = entity.customerEmail,
            customerFirstName = entity.customerFirstName,
            customerLastName = entity.customerLastName,
            customerPhone = entity.customerPhone,
            shippingAddress = entity.shippingAddress.toDto(),
            billingAddress = entity.billingAddress?.toDto(),
            subtotal = entity.subtotal,
            taxAmount = entity.taxAmount,
            shippingAmount = entity.shippingAmount,
            totalAmount = entity.totalAmount,
            status = entity.status,
            cartId = entity.cartId,
            notes = entity.notes,
            items = itemDtos,
            pdfUrl = generatePdfUrl(entity.id),
            createdAt = requireNotNull(entity.createdAt) { "Order createdAt cannot be null when converting to DTO" },
            updatedAt = requireNotNull(entity.updatedAt) { "Order updatedAt cannot be null when converting to DTO" },
        )
    }

    private fun convertOrderItems(items: List<OrderItem>): List<OrderItemDto> {
        if (items.isEmpty()) return emptyList()

        val lookupData = fetchLookupData(items)
        return items.map { it.toDto(lookupData) }
    }

    private fun fetchLookupData(items: List<OrderItem>): ItemLookupData {
        val articleIds = items.map { it.articleId }.distinct()
        val variantIds = items.map { it.variantId }.distinct()
        val imageIds = items.mapNotNull { it.generatedImageId }.distinct()

        return ItemLookupData(
            articles = articleQueryService.getArticlesByIds(articleIds),
            variants = articleQueryService.getMugVariantsByIds(variantIds),
            images = imageService.find(imageIds).mapValues { it.value as GeneratedImageDto },
        )
    }

    private data class ItemLookupData(
        val articles: Map<Long, ArticleDto>,
        val variants: Map<Long, MugArticleVariantDto>,
        val images: Map<Long, GeneratedImageDto>,
    )

    private fun OrderItem.toDto(lookupData: ItemLookupData): OrderItemDto =
        OrderItemDto(
            id = requireNotNull(id) { "OrderItem ID cannot be null when converting to DTO" },
            article =
                lookupData.articles[articleId]
                    ?: error("Missing ArticleDto for id: $articleId"),
            variant =
                lookupData.variants[variantId]
                    ?: error("Missing MugArticleVariantDto for id: $variantId"),
            quantity = quantity,
            pricePerItem = pricePerItem,
            totalPrice = totalPrice,
            generatedImageId = generatedImageId,
            generatedImageFilename = generatedImageId?.let { lookupData.images[it]?.filename },
            promptId = promptId,
            customData = customData,
            createdAt =
                requireNotNull(createdAt) {
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

    private fun AddressDto.toEntity(): Address =
        Address(
            streetAddress1 = streetAddress1,
            streetAddress2 = streetAddress2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
        )

    private fun Address.toDto(): AddressDto =
        AddressDto(
            streetAddress1 = streetAddress1,
            streetAddress2 = streetAddress2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
        )

    companion object {
        private const val TAX_RATE = 0.08 // 8%
        private const val SHIPPING_RATE_CENTS = 499L // $4.99
    }
}
