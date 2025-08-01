package com.jotoai.voenix.shop.domain.cart.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.domain.cart.assembler.CartAssembler
import com.jotoai.voenix.shop.domain.cart.dto.AddToCartRequest
import com.jotoai.voenix.shop.domain.cart.dto.CartDto
import com.jotoai.voenix.shop.domain.cart.dto.CartSummaryDto
import com.jotoai.voenix.shop.domain.cart.dto.UpdateCartItemRequest
import com.jotoai.voenix.shop.domain.cart.entity.Cart
import com.jotoai.voenix.shop.domain.cart.entity.CartItem
import com.jotoai.voenix.shop.domain.cart.enums.CartStatus
import com.jotoai.voenix.shop.domain.cart.exception.CartItemNotFoundException
import com.jotoai.voenix.shop.domain.cart.exception.CartNotFoundException
import com.jotoai.voenix.shop.domain.cart.exception.CartOperationException
import com.jotoai.voenix.shop.domain.cart.repository.CartRepository
import com.jotoai.voenix.shop.domain.users.entity.User
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository,
    private val mugVariantRepository: MugArticleVariantRepository,
    private val cartAssembler: CartAssembler,
) {
    private val logger = LoggerFactory.getLogger(CartService::class.java)

    /**
     * Gets or creates an active cart for the user
     */
    @Transactional
    fun getOrCreateActiveCart(userId: Long): CartDto {
        val user = findUserById(userId)

        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseGet {
                    logger.debug("Creating new cart for user: {}", userId)
                    createNewCart(user)
                }

        return cartAssembler.toDto(cart)
    }

    /**
     * Gets a cart summary (item count and total price)
     */
    @Transactional(readOnly = true)
    fun getCartSummary(userId: Long): CartSummaryDto {
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

    /**
     * Adds an item to the cart
     */
    @Transactional
    fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto {
        val user = findUserById(userId)
        val article = findArticleById(request.articleId)
        val variant = findVariantById(request.variantId)

        // Validate that the variant belongs to the article
        if (variant.article.id != article.id) {
            throw CartOperationException("Variant ${request.variantId} does not belong to article ${request.articleId}")
        }

        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseGet { createNewCart(user) }

        // Get current price from cost calculation
        val currentPrice = getCurrentPrice(article)

        // Create new cart item
        val cartItem =
            CartItem(
                cart = cart,
                article = article,
                variant = variant,
                quantity = request.quantity,
                priceAtTime = currentPrice,
                originalPrice = currentPrice,
                customData = request.customData,
            )

        // Add or update item in cart
        cart.addOrUpdateItem(cartItem)

        val savedCart =
            try {
                cartRepository.save(cart)
            } catch (e: OptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            } catch (e: ObjectOptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            }

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
    fun updateCartItem(
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
        request.customData?.let { cartItem.customData = it }

        val savedCart =
            try {
                cartRepository.save(cart)
            } catch (e: OptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            } catch (e: ObjectOptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            }

        logger.debug("Updated cart item: userId={}, itemId={}, quantity={}", userId, itemId, request.quantity)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Removes an item from the cart
     */
    @Transactional
    fun removeFromCart(
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

        val savedCart =
            try {
                cartRepository.save(cart)
            } catch (e: OptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            } catch (e: ObjectOptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            }

        logger.debug("Removed item from cart: userId={}, itemId={}", userId, itemId)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Clears all items from the cart
     */
    @Transactional
    fun clearCart(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

        cart.clearItems()

        val savedCart =
            try {
                cartRepository.save(cart)
            } catch (e: OptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            } catch (e: ObjectOptimisticLockingFailureException) {
                throw CartOperationException("Cart was modified by another operation. Please try again.", e)
            }

        logger.debug("Cleared cart for user: {}", userId)

        return cartAssembler.toDto(savedCart)
    }

    /**
     * Updates prices in active carts to current prices
     */
    @Transactional
    fun refreshCartPrices(userId: Long): CartDto {
        val cart =
            cartRepository
                .findActiveCartByUserId(userId)
                .orElseThrow { CartNotFoundException(userId, true) }

        var pricesUpdated = false

        cart.items.forEach { item ->
            val currentPrice = getCurrentPrice(item.article)
            if (item.originalPrice != currentPrice) {
                item.originalPrice = currentPrice
                pricesUpdated = true
            }
        }

        val savedCart =
            if (pricesUpdated) {
                try {
                    cartRepository.save(cart)
                } catch (e: OptimisticLockingFailureException) {
                    throw CartOperationException("Cart was modified by another operation. Please try again.", e)
                } catch (e: ObjectOptimisticLockingFailureException) {
                    throw CartOperationException("Cart was modified by another operation. Please try again.", e)
                }
            } else {
                cart
            }

        if (pricesUpdated) {
            logger.debug("Refreshed prices for cart: userId={}", userId)
        }

        return cartAssembler.toDto(savedCart)
    }

    private fun createNewCart(user: User): Cart {
        val cart =
            Cart(
                user = user,
                status = CartStatus.ACTIVE,
                expiresAt = OffsetDateTime.now().plusDays(DEFAULT_CART_EXPIRY_DAYS),
            )
        return cartRepository.save(cart)
    }

    private fun findUserById(userId: Long): User =
        userRepository
            .findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

    private fun findArticleById(articleId: Long): Article =
        articleRepository
            .findById(articleId)
            .orElseThrow { ResourceNotFoundException("Article not found with id: $articleId") }

    private fun findVariantById(variantId: Long): MugArticleVariant =
        mugVariantRepository
            .findById(variantId)
            .orElseThrow { ResourceNotFoundException("Variant not found with id: $variantId") }

    private fun getCurrentPrice(article: Article): Long {
        // Get price from cost calculation, default to 0 if not available
        return article.costCalculation?.salesTotalGross?.toLong() ?: 0L
    }

    companion object {
        private const val DEFAULT_CART_EXPIRY_DAYS = 30L
    }
}
