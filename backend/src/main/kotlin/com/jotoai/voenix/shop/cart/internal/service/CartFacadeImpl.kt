package com.jotoai.voenix.shop.cart.internal.service

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.cart.api.CartFacade
import com.jotoai.voenix.shop.cart.api.dto.AddToCartRequest
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.UpdateCartItemRequest
import com.jotoai.voenix.shop.cart.internal.assembler.CartAssembler
import com.jotoai.voenix.shop.cart.internal.assembler.OrderInfoAssembler
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.entity.CartItem
import com.jotoai.voenix.shop.cart.internal.repository.CartRepository
import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.image.api.exceptions.ImageAccessDeniedException
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Suppress("LongParameterList", "TooManyFunctions")
class CartFacadeImpl(
    private val cartRepository: CartRepository,
    private val userService: UserService,
    private val imageQueryService: ImageQueryService,
    private val promptQueryService: PromptQueryService,
    private val cartAssembler: CartAssembler,
    private val orderInfoAssembler: OrderInfoAssembler,
    private val articleQueryService: ArticleQueryService,
    private val cartInternalService: CartInternalService,
) : CartFacade {
    private val logger = KotlinLogging.logger {}

    /**
     * Adds an item to the cart
     */
    @Transactional
    override fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto {
        userService.getUserById(userId)
        validateAddToCartRequest(userId, request)

        val cart = cartInternalService.getOrCreateActiveCartEntity(userId)
        val currentPrice = articleQueryService.getCurrentGrossPrice(request.articleId)

        val cartItem = createCartItem(cart, request, currentPrice)
        cart.addOrUpdateItem(cartItem)

        val savedCart = saveCartWithOptimisticLocking(cart)
        logSuccessfulCartOperation(userId, request)

        return cartAssembler.toDto(savedCart)
    }

    private fun validateAddToCartRequest(
        userId: Long,
        request: AddToCartRequest,
    ) {
        validateArticleAndVariant(request)
        validateGeneratedImageForCart(userId, request)
        validatePromptForCart(request)
    }

    private fun validateArticleAndVariant(request: AddToCartRequest) {
        validateArticleExists(request.articleId)
        validateVariantAndAssociation(request.articleId, request.variantId)
    }

    private fun validateArticleExists(articleId: Long) {
        val articlesById = articleQueryService.getArticlesByIds(listOf(articleId))
        if (articleId !in articlesById) {
            throw ResourceNotFoundException("Article not found with id: $articleId")
        }
    }

    private fun validateVariantAndAssociation(
        articleId: Long,
        variantId: Long,
    ) {
        val variantsById = articleQueryService.getMugVariantsByIds(listOf(variantId))
        if (variantId !in variantsById) {
            throw ResourceNotFoundException("Variant not found with id: $variantId")
        }

        if (!articleQueryService.validateVariantBelongsToArticle(articleId, variantId)) {
            throw BadRequestException("Variant $variantId does not belong to article $articleId")
        }
    }

    private fun validateGeneratedImageForCart(
        userId: Long,
        request: AddToCartRequest,
    ) {
        request.generatedImageId?.let { imageId ->
            logger.debug {
                "Validating generated image for cart operation: userId=$userId, generatedImageId=$imageId, " +
                    "articleId=${request.articleId}, variantId=${request.variantId}"
            }

            if (!imageQueryService.validateGeneratedImageOwnership(imageId, userId)) {
                logger.warn {
                    "Generated image validation failed: userId=$userId, generatedImageId=$imageId, " +
                        "reason=ownership_check_failed"
                }

                if (!imageQueryService.existsGeneratedImageById(imageId)) {
                    throw ImageNotFoundException("Generated image not found with id: $imageId")
                } else {
                    throw ImageAccessDeniedException(
                        "You don't have permission to use this image",
                        userId,
                        imageId.toString(),
                    )
                }
            }

            logger.debug {
                "Generated image validation successful: userId=$userId, generatedImageId=$imageId"
            }
        }
    }

    private fun validatePromptForCart(request: AddToCartRequest) {
        request.promptId?.let { promptId ->
            if (!promptQueryService.existsById(promptId)) {
                throw ResourceNotFoundException("Prompt not found with id: $promptId")
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

    private fun logSuccessfulCartOperation(
        userId: Long,
        request: AddToCartRequest,
    ) {
        logger.info {
            "Successfully added item to cart: " + "userId=$userId, " + "articleId=${request.articleId}, " +
                "variantId=${request.variantId}, " +
                "quantity=${request.quantity}, " +
                "generatedImageId=${request.generatedImageId}, " +
                "promptId=${request.promptId}, " +
                "hasCustomData=${request.customData.isNotEmpty()}"
        }
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
            logger.debug {
                "Validating generated image for cart update: userId=$userId, itemId=$itemId, generatedImageId=$imageId"
            }

            if (!imageQueryService.validateGeneratedImageOwnership(imageId, userId)) {
                logger.warn {
                    "Generated image validation failed during cart update: userId=$userId, itemId=$itemId, " +
                        "generatedImageId=$imageId, reason=ownership_check_failed"
                }

                // First check if image exists at all
                if (!imageQueryService.existsGeneratedImageById(imageId)) {
                    throw ImageNotFoundException("Generated image not found with id: $imageId")
                } else {
                    throw ImageAccessDeniedException(
                        "You don't have permission to use this image",
                        userId,
                        imageId.toString(),
                    )
                }
            }

            logger.debug {
                "Generated image validation successful for cart update: userId=$userId, itemId=$itemId, " +
                    "generatedImageId=$imageId"
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

        logger.info {
            "Successfully updated cart item: userId=$userId, itemId=$itemId, quantity=${request.quantity}, " +
                "generatedImageId=${cartItem.generatedImageId}, promptId=${cartItem.promptId}, " +
                "hasCustomData=${request.customData != null}"
        }

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
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        // Log details about the item being removed before removal
        val itemToRemove = cart.items.find { it.id == itemId }
        itemToRemove?.let { item ->
            logger.debug {
                "Removing cart item: userId=$userId, itemId=$itemId, articleId=${item.articleId}, " +
                    "variantId=${item.variantId}, generatedImageId=${item.generatedImageId}, " +
                    "promptId=${item.promptId}"
            }
        }

        if (!cart.removeItem(itemId)) {
            throw ResourceNotFoundException("Cart item $itemId not found in cart ${cart.id}")
        }

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info {
            "Successfully removed item from cart: userId=$userId, itemId=$itemId, " +
                "hadGeneratedImage=${itemToRemove?.generatedImageId != null}"
        }

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
                .orElseThrow { ResourceNotFoundException("Active cart not found for user: $userId") }

        // Log details about items being cleared
        val itemsWithGeneratedImages = cart.items.count { it.generatedImageId != null }
        val totalItems = cart.items.size

        logger.debug {
            "Clearing cart contents: userId=$userId, totalItems=$totalItems, " +
                "itemsWithGeneratedImages=$itemsWithGeneratedImages"
        }

        cart.clearItems()

        val savedCart = saveCartWithOptimisticLocking(cart)

        logger.info {
            "Successfully cleared cart: userId=$userId, clearedItems=$totalItems, " +
                "clearedItemsWithGeneratedImages=$itemsWithGeneratedImages"
        }

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

    /**
     * Refreshes cart prices for order creation.
     * Updates all item prices to current prices and returns cart order info.
     */
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
