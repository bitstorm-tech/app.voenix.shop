package com.jotoai.voenix.shop.cart.internal.assembler

import com.jotoai.voenix.shop.article.ArticleDto
import com.jotoai.voenix.shop.article.ArticleService
import com.jotoai.voenix.shop.article.ArticleType
import com.jotoai.voenix.shop.article.MugArticleVariantDto
import com.jotoai.voenix.shop.cart.CartDto
import com.jotoai.voenix.shop.cart.CartItemDto
import com.jotoai.voenix.shop.cart.CartStatus
import com.jotoai.voenix.shop.cart.internal.entity.Cart
import com.jotoai.voenix.shop.cart.internal.entity.CartItem
import com.jotoai.voenix.shop.image.GeneratedImageDto
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

class CartAssemblerTest {
    private lateinit var articleService: ArticleService
    private lateinit var imageService: ImageService
    private lateinit var cartAssembler: CartAssembler

    @BeforeEach
    fun setUp() {
        articleService = mockk()
        imageService = mockk()
        cartAssembler = CartAssembler(articleService, imageService)
    }

    @Test
    fun `toDto should convert cart with items containing generated images`() {
        // Given
        val cartId = 1L
        val userId = 100L
        val cart = createCart(id = cartId, userId = userId)
        val (item1, item2) = setupCartItemsWithImages(cart)
        setupMockServiceResponses()

        // When
        val result = cartAssembler.toDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(cartId, result.id)
        assertEquals(userId, result.userId)
        assertEquals(2, result.items.size)

        verifyFirstCartItem(result.items[0])
        verifySecondCartItem(result.items[1])
        verifyCartTotals(result)
    }

    private fun setupCartItemsWithImages(cart: Cart): Pair<CartItem, CartItem> {
        val item1 =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                    generatedImageId = 3001L,
                    promptId = 4001L,
                    quantity = 2,
                    priceAtTime = 1999L,
                ),
            )
        val item2 =
            createCartItem(
                CartItemConfig(
                    id = 11L,
                    articleId = 1002L,
                    variantId = 2002L,
                    generatedImageId = 3002L,
                    promptId = 4002L,
                    quantity = 1,
                    priceAtTime = 2499L,
                ),
            )
        cart.items.add(item1)
        cart.items.add(item2)
        item1.cart = cart
        item2.cart = cart
        return Pair(item1, item2)
    }

    private fun setupMockServiceResponses() {
        val article1 = createArticleDto(id = 1001L, name = "Custom Mug 1")
        val article2 = createArticleDto(id = 1002L, name = "Custom Mug 2")
        val variant1 = createMugVariantDto(id = 2001L, articleId = 1001L, colorCode = "RED")
        val variant2 = createMugVariantDto(id = 2002L, articleId = 1002L, colorCode = "BLUE")
        val image1 = createGeneratedImageDto(filename = "image1.jpg", promptId = 4001L)
        val image2 = createGeneratedImageDto(filename = "image2.jpg", promptId = 4002L)

        every { articleService.getArticlesByIds(listOf(1001L, 1002L)) } returns
            mapOf(
                1001L to article1,
                1002L to article2,
            )
        every { articleService.getMugVariantsByIds(listOf(2001L, 2002L)) } returns
            mapOf(
                2001L to variant1,
                2002L to variant2,
            )
        every { imageService.find(listOf(3001L, 3002L)) } returns
            mapOf(
                3001L to image1,
                3002L to image2,
            )
    }

    private fun verifyFirstCartItem(item: CartItemDto) {
        assertEquals(10L, item.id)
        assertEquals(3001L, item.generatedImageId)
        assertEquals("image1.jpg", item.generatedImageFilename)
        assertEquals(4001L, item.promptId)
        assertEquals(2, item.quantity)
        assertEquals(3998L, item.totalPrice)
    }

    private fun verifySecondCartItem(item: CartItemDto) {
        assertEquals(11L, item.id)
        assertEquals(3002L, item.generatedImageId)
        assertEquals("image2.jpg", item.generatedImageFilename)
        assertEquals(4002L, item.promptId)
        assertEquals(1, item.quantity)
        assertEquals(2499L, item.totalPrice)
    }

    private fun verifyCartTotals(result: CartDto) {
        assertEquals(3, result.totalItemCount)
        assertEquals(6497L, result.totalPrice)
        assertFalse(result.isEmpty)
    }

    @Test
    fun `toDto should handle cart items without generated images`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)

        val item =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                    generatedImageId = null,
                    promptId = null,
                    quantity = 1,
                    priceAtTime = 1999L,
                ),
            )

        cart.items.add(item)
        item.cart = cart

        val article = createArticleDto(id = 1001L, name = "Basic Mug")
        val variant = createMugVariantDto(id = 2001L, articleId = 1001L, colorCode = "WHITE")

        every { articleService.getArticlesByIds(listOf(1001L)) } returns
            mapOf(
                1001L to article,
            )
        every { articleService.getMugVariantsByIds(listOf(2001L)) } returns
            mapOf(
                2001L to variant,
            )
        every { imageService.find(emptyList()) } returns emptyMap()

        // When
        val result = cartAssembler.toDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(1, result.items.size)

        val resultItem = result.items[0]
        assertNull(resultItem.generatedImageId)
        assertNull(resultItem.generatedImageFilename)
        assertNull(resultItem.promptId)
    }

    @Test
    fun `toDto should handle mixed items with and without generated images`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)

        val itemWithImage =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                    generatedImageId = 3001L,
                    promptId = 4001L,
                    quantity = 1,
                    priceAtTime = 1999L,
                ),
            )

        val itemWithoutImage =
            createCartItem(
                CartItemConfig(
                    id = 11L,
                    articleId = 1002L,
                    variantId = 2002L,
                    generatedImageId = null,
                    promptId = null,
                    quantity = 2,
                    priceAtTime = 1499L,
                ),
            )

        cart.items.add(itemWithImage)
        cart.items.add(itemWithoutImage)
        itemWithImage.cart = cart
        itemWithoutImage.cart = cart

        val article1 = createArticleDto(id = 1001L, name = "Custom Mug")
        val article2 = createArticleDto(id = 1002L, name = "Basic Mug")
        val variant1 = createMugVariantDto(id = 2001L, articleId = 1001L, colorCode = "RED")
        val variant2 = createMugVariantDto(id = 2002L, articleId = 1002L, colorCode = "BLUE")

        val image = createGeneratedImageDto(filename = "custom-image.jpg", promptId = 4001L)

        every { articleService.getArticlesByIds(listOf(1001L, 1002L)) } returns
            mapOf(
                1001L to article1,
                1002L to article2,
            )
        every { articleService.getMugVariantsByIds(listOf(2001L, 2002L)) } returns
            mapOf(
                2001L to variant1,
                2002L to variant2,
            )
        every { imageService.find(listOf(3001L)) } returns mapOf(3001L to image)

        // When
        val result = cartAssembler.toDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(2, result.items.size)

        // Item with image
        val itemWithImageResult = result.items[0]
        assertEquals(3001L, itemWithImageResult.generatedImageId)
        assertEquals("custom-image.jpg", itemWithImageResult.generatedImageFilename)

        // Item without image
        val itemWithoutImageResult = result.items[1]
        assertNull(itemWithoutImageResult.generatedImageId)
        assertNull(itemWithoutImageResult.generatedImageFilename)
    }

    @Test
    fun `toDto should handle missing generated image gracefully`() {
        // Given - Item has generatedImageId but image is not found
        val cart = createCart(id = 1L, userId = 100L)

        val item =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                    generatedImageId = 3001L,
                    promptId = 4001L,
                    quantity = 1,
                    priceAtTime = 1999L,
                ),
            )

        cart.items.add(item)
        item.cart = cart

        val article = createArticleDto(id = 1001L, name = "Custom Mug")
        val variant = createMugVariantDto(id = 2001L, articleId = 1001L, colorCode = "RED")

        every { articleService.getArticlesByIds(listOf(1001L)) } returns
            mapOf(
                1001L to article,
            )
        every { articleService.getMugVariantsByIds(listOf(2001L)) } returns
            mapOf(
                2001L to variant,
            )
        every { imageService.find(listOf(3001L)) } returns emptyMap() // Image not found

        // When
        val result = cartAssembler.toDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(1, result.items.size)

        val resultItem = result.items[0]
        assertEquals(3001L, resultItem.generatedImageId)
        assertNull(resultItem.generatedImageFilename) // Should be null when image not found
        assertEquals(4001L, resultItem.promptId)
    }

    @Test
    fun `toDto should batch load images efficiently`() {
        // Given - Multiple items with same image ID
        val cart = createCart(id = 1L, userId = 100L)
        val items = setupBatchLoadTestItems(cart)
        setupBatchLoadMockResponses()

        // When
        val result = cartAssembler.toDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(3, result.items.size)
        verifyBatchLoadResults(result)
    }

    private fun setupBatchLoadTestItems(cart: Cart): List<CartItem> {
        val item1 =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                    generatedImageId = 3001L,
                    quantity = 1,
                    priceAtTime = 1999L,
                ),
            )
        val item2 =
            createCartItem(
                CartItemConfig(
                    id = 11L,
                    articleId = 1002L,
                    variantId = 2002L,
                    generatedImageId = 3001L,
                    quantity = 1,
                    priceAtTime = 2499L,
                ),
            )
        val item3 =
            createCartItem(
                CartItemConfig(
                    id = 12L,
                    articleId = 1003L,
                    variantId = 2003L,
                    generatedImageId = 3002L,
                    quantity = 1,
                    priceAtTime = 1899L,
                ),
            )

        val items = listOf(item1, item2, item3)
        cart.items.addAll(items)
        items.forEach { it.cart = cart }
        return items
    }

    private fun setupBatchLoadMockResponses() {
        val articles =
            mapOf(
                1001L to createArticleDto(id = 1001L, name = "Mug 1"),
                1002L to createArticleDto(id = 1002L, name = "Mug 2"),
                1003L to createArticleDto(id = 1003L, name = "Mug 3"),
            )
        val variants =
            mapOf(
                2001L to createMugVariantDto(id = 2001L, articleId = 1001L),
                2002L to createMugVariantDto(id = 2002L, articleId = 1002L),
                2003L to createMugVariantDto(id = 2003L, articleId = 1003L),
            )
        val images =
            mapOf(
                3001L to createGeneratedImageDto(filename = "shared-image.jpg"),
                3002L to createGeneratedImageDto(filename = "unique-image.jpg"),
            )

        every { articleService.getArticlesByIds(listOf(1001L, 1002L, 1003L)) } returns articles
        every { articleService.getMugVariantsByIds(listOf(2001L, 2002L, 2003L)) } returns variants
        every { imageService.find(listOf(3001L, 3002L)) } returns images
    }

    private fun verifyBatchLoadResults(result: CartDto) {
        assertEquals("shared-image.jpg", result.items[0].generatedImageFilename)
        assertEquals("shared-image.jpg", result.items[1].generatedImageFilename)
        assertEquals("unique-image.jpg", result.items[2].generatedImageFilename)
    }

    @Test
    fun `toDto should throw exception when cart ID is null`() {
        // Given
        val cart = createCart(id = null, userId = 100L)

        // When & Then
        val exception =
            assertThrows<IllegalArgumentException> {
                cartAssembler.toDto(cart)
            }
        assertEquals("Cart ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto should throw exception when article is not found`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)
        val item =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                ),
            )
        cart.items.add(item)
        item.cart = cart

        every { articleService.getArticlesByIds(listOf(1001L)) } returns emptyMap()
        every { articleService.getMugVariantsByIds(listOf(2001L)) } returns
            mapOf(
                2001L to createMugVariantDto(id = 2001L, articleId = 1001L),
            )
        every { imageService.find(emptyList()) } returns emptyMap()

        // When & Then
        val exception =
            assertThrows<IllegalStateException> {
                cartAssembler.toDto(cart)
            }
        assertEquals("Missing ArticleDto for id: 1001", exception.message)
    }

    @Test
    fun `toDto should throw exception when variant is not found`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)
        val item =
            createCartItem(
                CartItemConfig(
                    id = 10L,
                    articleId = 1001L,
                    variantId = 2001L,
                ),
            )
        cart.items.add(item)
        item.cart = cart

        every { articleService.getArticlesByIds(listOf(1001L)) } returns
            mapOf(
                1001L to createArticleDto(id = 1001L, name = "Mug"),
            )
        every { articleService.getMugVariantsByIds(listOf(2001L)) } returns emptyMap()
        every { imageService.find(emptyList()) } returns emptyMap()

        // When & Then
        val exception =
            assertThrows<IllegalStateException> {
                cartAssembler.toDto(cart)
            }
        assertEquals("Missing MugArticleVariantDto for id: 2001", exception.message)
    }

    @Test
    fun `toSummaryDto should create correct summary`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)

        val item1 = createCartItem(CartItemConfig(quantity = 2, priceAtTime = 1999L))
        val item2 = createCartItem(CartItemConfig(quantity = 1, priceAtTime = 2499L))

        cart.items.add(item1)
        cart.items.add(item2)
        item1.cart = cart
        item2.cart = cart

        // When
        val result = cartAssembler.toSummaryDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(3, result.itemCount) // 2 + 1
        assertEquals(6497L, result.totalPrice) // (2 * 1999) + 2499
        assertTrue(result.hasItems)
    }

    @Test
    fun `toSummaryDto should handle empty cart`() {
        // Given
        val cart = createCart(id = 1L, userId = 100L)

        // When
        val result = cartAssembler.toSummaryDto(cart)

        // Then
        assertNotNull(result)
        assertEquals(0, result.itemCount)
        assertEquals(0L, result.totalPrice)
        assertFalse(result.hasItems)
    }

    // Helper methods
    private fun createCart(
        id: Long? = 1L,
        userId: Long = 100L,
        status: CartStatus = CartStatus.ACTIVE,
        version: Long = 0L,
    ): Cart =
        Cart(
            id = id,
            userId = userId,
            status = status,
            version = version,
            expiresAt = OffsetDateTime.now().plusDays(7),
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    private data class CartItemConfig(
        val id: Long = 1L,
        val articleId: Long = 1001L,
        val variantId: Long = 2001L,
        val quantity: Int = 1,
        val priceAtTime: Long = 1999L,
        val generatedImageId: Long? = null,
        val promptId: Long? = null,
        val position: Int = 0,
    )

    private fun createCartItem(config: CartItemConfig = CartItemConfig()): CartItem {
        val tempCart = createCart(id = 999L, userId = 999L)
        return CartItem(
            id = config.id,
            cart = tempCart,
            articleId = config.articleId,
            variantId = config.variantId,
            quantity = config.quantity,
            priceAtTime = config.priceAtTime,
            originalPrice = config.priceAtTime,
            customData = emptyMap(),
            generatedImageId = config.generatedImageId,
            promptId = config.promptId,
            position = config.position,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )
    }

    private fun createArticleDto(
        id: Long,
        name: String,
        articleType: ArticleType = ArticleType.MUG,
    ): ArticleDto =
        ArticleDto(
            id = id,
            name = name,
            descriptionShort = "Short description for $name",
            descriptionLong = "Long description for $name",
            active = true,
            articleType = articleType,
            categoryId = 1L,
            categoryName = "Test Category",
            subcategoryId = 1L,
            subcategoryName = "Test Subcategory",
            supplierId = 1L,
            supplierName = "Test Supplier",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    private fun createMugVariantDto(
        id: Long,
        articleId: Long,
        colorCode: String = "WHITE",
    ): MugArticleVariantDto =
        MugArticleVariantDto(
            id = id,
            articleId = articleId,
            insideColorCode = colorCode,
            outsideColorCode = colorCode,
            name = "${colorCode.lowercase().replaceFirstChar { it.uppercase() }} Mug",
            exampleImageUrl = null,
            articleVariantNumber = "VAR-$id",
            isDefault = colorCode == "WHITE",
            active = true,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    private fun createGeneratedImageDto(
        filename: String,
        promptId: Long = 4001L,
        userId: Long? = 100L,
    ): GeneratedImageDto =
        GeneratedImageDto(
            filename = filename,
            imageType = ImageType.GENERATED,
            promptId = promptId,
            userId = userId,
            createdAt = OffsetDateTime.now(),
            ipAddress = "127.0.0.1",
        )
}
