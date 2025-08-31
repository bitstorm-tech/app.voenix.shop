package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.cart.api.CartService
import com.jotoai.voenix.shop.cart.api.dto.AddToCartRequest
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.CartSummaryDto
import com.jotoai.voenix.shop.cart.api.dto.UpdateCartItemRequest
import com.jotoai.voenix.shop.cart.internal.assembler.CartAssembler
import com.jotoai.voenix.shop.cart.internal.assembler.OrderInfoAssembler
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.entity.CartItem
import com.jotoai.voenix.shop.cart.api.enums.CartStatus
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import com.jotoai.voenix.shop.image.ImageException
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ValidationRequest
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Suppress("LongParameterList", "TooManyFunctions")
class CartServiceImpl(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val imageService: ImageService,
    private val promptQueryService: PromptQueryService,
    private val cartAssembler: CartAssembler,
    private val orderInfoAssembler: OrderInfoAssembler,
    private val articleQueryService: ArticleQueryService,
) : CartService {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DEFAULT_CART_EXPIRY_DAYS = 30L
    }

    @Transactional(readOnly = true)
    override fun getActiveCartForOrder(userId: Long): CartOrderInfo? {
        val cart = cartRepository.findActiveCartByUserId(userId).orElse(null)
        return cart?.let { orderInfoAssembler.toOrderInfo(it) }
    }

    @Transactional
    override fun refreshCartPricesForOrder(cartId: Long): CartOrderInfo {
        val cart =
            cartRepository
                .findById(cartId)
                .orElseThrow { ResourceNotFoundException("Cart not found with id: $cartId") }

        logger.debug {
            "Refreshing cart prices for order: cartId=$cartId, userId=${cart.userId}, " +
                "itemCount=${cart.items.size}, itemsWithGeneratedImages=${
                    cart.items.count {
                        it.generatedImageId != null
                    }
                }"
        }

        // Update prices to current
        cart.items.forEach { item ->
            val currentPrice = articleQueryService.getCurrentGrossPrice(item.articleId)
            if (item.priceAtTime != currentPrice) {
                logger.warn {
                    "Price changed for cart item ${item.id}: ${item.priceAtTime} -> $currentPrice"
                }
                item.priceAtTime = currentPrice
            }
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        // Return cart order info
        return orderInfoAssembler.toOrderInfo(savedCart)
    }

    @Transactional
    override fun markCartAsConverted(cartId: Long) {
        val cart =
            cartRepository
                .findById(cartId)
                .orElseThrow { ResourceNotFoundException("Cart not found with id: $cartId") }

        cart.status = CartStatus.CONVERTED
        cartRepository.save(cart)

        logger.info { "Cart marked as converted: cartId=$cartId" }
    }

    @Transactional
    override fun getOrCreateActiveCart(userId: Long): CartDto {
        // Validate user exists
        userService.getUserById(userId)

        val cart = getOrCreateActiveCartEntity(userId)
        return cartAssembler.toDto(cart)
    }

    @Transactional(readOnly = true)
    override fun getCartSummary(userId: Long): CartSummaryDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElse(null)

        return if (cart != null) {
            cartAssembler.toSummaryDto(cart)
        } else {
            CartSummaryDto(itemCount = 0, totalPrice = 0L, hasItems = false)
        }
    }

    @Transactional
    override fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto {
        userService.getUserById(userId)
        validateAddToCartRequest(userId, request)

        val cart = getOrCreateActiveCartEntity(userId)
        val currentPrice = articleQueryService.getCurrentGrossPrice(request.articleId)

        val cartItem = createCartItem(cart, request, currentPrice)
        cart.addOrUpdateItem(cartItem)

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info { "Cart operation: action=add, userId=$userId, articleId=${request.articleId}" }

        return cartAssembler.toDto(savedCart)
    }

    @Transactional
    override fun updateCartItem(
        userId: Long,
        itemId: Long,
        request: UpdateCartItemRequest,
    ): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        val cartItem =
            cart.items.find { it.id == itemId }
                ?: throw ResourceNotFoundException("Cart item $itemId not found in cart ${cart.id}")

        // Update the cart item
        cartItem.updateQuantity(request.quantity)

        // Update custom data (only for crop data and similar non-FK fields)
        request.customData?.let { newCustomData ->
            cartItem.customData = newCustomData
        }

        // Update FK fields directly
        request.generatedImageId?.let { imageId ->
            validateAndGetImageId(imageId, userId)
            cartItem.generatedImageId = imageId
        }

        request.promptId?.let { promptId ->
            if (!promptQueryService.existsById(promptId)) {
                throw ResourceNotFoundException("Prompt not found with id: $promptId")
            }
            cartItem.promptId = promptId
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info {
            "Cart operation: action=update, userId=$userId, itemId=$itemId, quantity=${request.quantity}"
        }

        return cartAssembler.toDto(savedCart)
    }

    @Transactional
    override fun removeFromCart(
        userId: Long,
        itemId: Long,
    ): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        // Log details about the item being removed before removal
        val itemToRemove = cart.items.find { it.id == itemId }
        itemToRemove?.let { item ->
            logger.debug {
                "Removing cart item: userId=$userId, itemId=$itemId, articleId=${item.articleId}"
            }
        }

        if (!cart.removeItem(itemId)) {
            throw ResourceNotFoundException("Cart item $itemId not found in cart ${cart.id}")
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info {
            "Cart operation: action=remove, userId=$userId, itemId=$itemId"
        }

        return cartAssembler.toDto(savedCart)
    }

    @Transactional
    override fun clearCart(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        // Log details about items being cleared
        val totalItems = cart.items.size

        logger.debug { "Clearing cart contents: userId=$userId, totalItems=$totalItems" }

        cart.clearItems()

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info { "Cart operation: action=clear, userId=$userId, clearedItems=$totalItems" }

        return cartAssembler.toDto(savedCart)
    }

    @Transactional
    override fun refreshCartPrices(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        var pricesUpdated = false

        cart.items.forEach { item ->
            val currentPrice = articleQueryService.getCurrentGrossPrice(item.articleId)
            if (item.originalPrice != currentPrice) {
                item.originalPrice = currentPrice
                pricesUpdated = true
            }
        }

        val savedCart =
            if (pricesUpdated) {
                saveCartWithOptimisticLocking(cart)
            } else {
                cart
            }

        if (pricesUpdated) {
            logger.debug { "Refreshed prices for cart: userId=$userId" }
        }

        return cartAssembler.toDto(savedCart)
    }

    private fun getOrCreateActiveCartEntity(userId: Long): Cart =
        cartRepository
            .findActiveCartByUserId(userId)
            .orElseGet {
                logger.debug { "Creating new cart for user: $userId" }
                val cart =
                    Cart(
                        userId = userId,
                        status = CartStatus.ACTIVE,
                        expiresAt = OffsetDateTime.now().plusDays(DEFAULT_CART_EXPIRY_DAYS),
                    )
                cartRepository.save(cart)
            }

    private fun validateAddToCartRequest(
        userId: Long,
        request: AddToCartRequest,
    ) {
        validateArticleAndVariant(request)
        validateImageAndPrompt(userId, request)
    }

    private fun validateArticleAndVariant(request: AddToCartRequest) {
        if (request.articleId !in articleQueryService.getArticlesByIds(listOf(request.articleId))) {
            throw ResourceNotFoundException("Article not found with id: ${request.articleId}")
        }
        if (request.variantId !in articleQueryService.getMugVariantsByIds(listOf(request.variantId))) {
            throw ResourceNotFoundException("Variant not found with id: ${request.variantId}")
        }
        if (!articleQueryService.validateVariantBelongsToArticle(request.articleId, request.variantId)) {
            throw BadRequestException("Variant ${request.variantId} does not belong to article ${request.articleId}")
        }
    }

    private fun validateImageAndPrompt(
        userId: Long,
        request: AddToCartRequest,
    ) {
        validateAndGetImageId(request.generatedImageId, userId)
        request.promptId?.let {
            if (!promptQueryService.existsById(it)) {
                throw ResourceNotFoundException("Prompt not found with id: $it")
            }
        }
    }

    private fun validateAndGetImageId(
        imageId: Long?,
        userId: Long,
    ): Long? =
        imageId?.also {
            val validation = imageService.validate(ValidationRequest.Ownership(it, userId))
            if (!validation.valid) {
                val exists = imageService.find(listOf(it)).isNotEmpty()
                throw if (exists) {
                    ImageException.AccessDenied(userId, it.toString())
                } else {
                    ImageException.NotFound(it.toString())
                }
            }
        }

    private fun createCartItem(
        cart: Cart,
        request: AddToCartRequest,
        currentPrice: Long,
    ): CartItem =
        CartItem(
            cart = cart,
            articleId = request.articleId,
            variantId = request.variantId,
            quantity = request.quantity,
            priceAtTime = currentPrice,
            originalPrice = currentPrice,
            customData = request.customData,
            generatedImageId = request.generatedImageId,
            promptId = request.promptId,
        )

    private fun saveCartWithOptimisticLocking(cart: Cart): Cart =
        try {
            cartRepository.save(cart)
        } catch (e: OptimisticLockingFailureException) {
            throw BadRequestException(
                "Cart was modified by another operation. Please try again.",
                e,
            )
        }
}
