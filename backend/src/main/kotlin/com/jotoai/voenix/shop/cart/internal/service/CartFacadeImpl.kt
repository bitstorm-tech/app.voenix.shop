package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.cart.api.CartFacade
import com.jotoai.voenix.shop.cart.api.dto.AddToCartRequest
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.CartOrderItemInfo
import com.jotoai.voenix.shop.cart.api.dto.UpdateCartItemRequest
import com.jotoai.voenix.shop.cart.api.exceptions.CartItemNotFoundException
import com.jotoai.voenix.shop.cart.api.exceptions.CartNotFoundException
import com.jotoai.voenix.shop.cart.api.exceptions.CartOperationException
import com.jotoai.voenix.shop.cart.internal.assembler.CartAssembler
import com.jotoai.voenix.shop.cart.internal.entity.CartItem
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserQueryService
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartFacadeImpl(
    private val cartRepository: CartRepository,
    private val userQueryService: UserQueryService,
    private val imageQueryService: ImageQueryService,
    private val promptQueryService: PromptQueryService,
    private val cartAssembler: CartAssembler,
    private val articleQueryService: ArticleQueryService,
    private val cartInternalService: CartInternalService,
) : CartFacade {
    private val logger = LoggerFactory.getLogger(CartFacadeImpl::class.java)

    /**
     * Adds an item to the cart
     */
    @Transactional
    override fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto {
        // Validate user exists
        userQueryService.getUserById(userId)

        // Validate article and variant exist using ArticleQueryService
        val articlesById = articleQueryService.getArticlesByIds(listOf(request.articleId))
        if (request.articleId !in articlesById) {
            throw ResourceNotFoundException("Article not found with id: ${request.articleId}")
        }

        val variantsById = articleQueryService.getMugVariantsByIds(listOf(request.variantId))
        if (request.variantId !in variantsById) {
            throw ResourceNotFoundException("Variant not found with id: ${request.variantId}")
        }

        // Validate that the variant belongs to the article
        if (!articleQueryService.validateVariantBelongsToArticle(request.articleId, request.variantId)) {
            throw CartOperationException("Variant ${request.variantId} does not belong to article ${request.articleId}")
        }

        val cart = cartInternalService.getOrCreateActiveCartEntity(userId)

        // Get current price from cost calculation
        val currentPrice = articleQueryService.getCurrentGrossPrice(request.articleId)

        // Validate related entities if provided
        request.generatedImageId?.let { imageId ->
            if (!imageQueryService.existsGeneratedImageById(imageId)) {
                throw ResourceNotFoundException("Generated image not found with id: $imageId")
            }
        }

        // Validate prompt if provided
        request.promptId?.let { promptId ->
            if (!promptQueryService.existsById(promptId)) {
                throw ResourceNotFoundException("Prompt not found with id: $promptId")
            }
        }

        // Create new cart item
        val cartItem =
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

        // Add or update item in cart
        cart.addOrUpdateItem(cartItem)

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.debug(
            "Added item to cart: userId={}, articleId={}, variantId={}, quantity={}",
            userId,
            request.articleId,
            request.variantId,
            request.quantity,
        )

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Updates a cart item quantity or custom data
     */
    @Transactional
    override fun updateCartItem(
        userId: Long,
        itemId: Long,
        request: UpdateCartItemRequest,
    ): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

        val cartItem =
            cart.items.find { it.id == itemId }
                ?: throw CartItemNotFoundException(cart.id!!, itemId)

        // Update the cart item
        cartItem.updateQuantity(request.quantity)

        // Update custom data (only for crop data and similar non-FK fields)
        request.customData?.let { newCustomData ->
            cartItem.customData = newCustomData
        }

        // Update FK fields directly
        request.generatedImageId?.let { imageId ->
            if (!imageQueryService.existsGeneratedImageById(imageId)) {
                throw ResourceNotFoundException("Generated image not found with id: $imageId")
            }
            cartItem.generatedImageId = imageId
        }

        request.promptId?.let { promptId ->
            if (!promptQueryService.existsById(promptId)) {
                throw ResourceNotFoundException("Prompt not found with id: $promptId")
            }
            cartItem.promptId = promptId
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.debug("Updated cart item: userId={}, itemId={}, quantity={}", userId, itemId, request.quantity)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Removes an item from the cart
     */
    @Transactional
    override fun removeFromCart(
        userId: Long,
        itemId: Long,
    ): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

        if (!cart.removeItem(itemId)) {
            throw CartItemNotFoundException(cart.id!!, itemId)
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.debug("Removed item from cart: userId={}, itemId={}", userId, itemId)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Clears all items from the cart
     */
    @Transactional
    override fun clearCart(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

        cart.clearItems()

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.debug("Cleared cart for user: {}", userId)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Updates prices in active carts to current prices
     */
    @Transactional
    override fun refreshCartPrices(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

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
            logger.debug("Refreshed prices for cart: userId={}", userId)
        }

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Refreshes cart prices for order creation.
     * Updates all item prices to current prices and returns cart order info.
     */
    @Transactional
    override fun refreshCartPricesForOrder(cartId: Long): CartOrderInfo {
        val cart =
            cartRepository
                .findById(cartId)
                .orElseThrow { CartNotFoundException(userId = 0, isActiveCart = false) }

        // Update prices to current
        cart.items.forEach { item ->
            val currentPrice = articleQueryService.getCurrentGrossPrice(item.articleId)
            if (item.priceAtTime != currentPrice) {
                logger.warn(
                    "Price changed for cart item {}: {} -> {}",
                    item.id,
                    item.priceAtTime,
                    currentPrice,
                )
                item.priceAtTime = currentPrice
            }
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        // Return cart order info
        return CartOrderInfo(
            id = savedCart.id!!,
            userId = savedCart.userId,
            status = savedCart.status,
            items =
                savedCart.items.map { item ->
                    CartOrderItemInfo(
                        id = item.id!!,
                        articleId = item.articleId,
                        variantId = item.variantId,
                        quantity = item.quantity,
                        priceAtTime = item.priceAtTime,
                        totalPrice = item.getTotalPrice(),
                        generatedImageId = item.generatedImageId,
                        promptId = item.promptId,
                        promptText = item.promptId?.let { 
                            try {
                                promptQueryService.getPromptById(it).promptText
                            } catch (e: Exception) {
                                null // Handle case where prompt might have been deleted
                            }
                        },
                        customData = item.customData,
                    )
                },
            totalPrice = savedCart.getTotalPrice(),
            isEmpty = savedCart.isEmpty(),
        )
    }

    private fun saveCartWithOptimisticLocking(
        cart: com.jotoai.voenix.shop.cart.internal.entity.Cart,
    ): com.jotoai.voenix.shop.cart.internal.entity.Cart =
        try {
            cartRepository.save(cart)
        } catch (e: OptimisticLockingFailureException) {
            throw CartOperationException("Cart was modified by another operation. Please try again.", e)
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw CartOperationException("Cart was modified by another operation. Please try again.", e)
        }
}
