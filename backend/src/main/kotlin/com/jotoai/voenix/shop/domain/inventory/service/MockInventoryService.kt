package com.jotoai.voenix.shop.domain.inventory.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Mock implementation of InventoryService for development.
 * Always returns 100 items in stock and successful operations.
 */
@Service
class MockInventoryService : InventoryService {
    private val logger = LoggerFactory.getLogger(MockInventoryService::class.java)

    override fun isInStock(
        variantId: Long,
        quantity: Int,
    ): Boolean {
        logger.debug("Mock inventory check: variantId={}, quantity={} - returning true", variantId, quantity)
        return quantity <= MOCK_STOCK_LEVEL
    }

    override fun getStockLevel(variantId: Long): Int {
        logger.debug("Mock stock level check: variantId={} - returning {}", variantId, MOCK_STOCK_LEVEL)
        return MOCK_STOCK_LEVEL
    }

    override fun reserveStock(
        variantId: Long,
        quantity: Int,
    ): Boolean {
        logger.debug("Mock stock reservation: variantId={}, quantity={} - returning true", variantId, quantity)
        return quantity <= MOCK_STOCK_LEVEL
    }

    override fun releaseStock(
        variantId: Long,
        quantity: Int,
    ) {
        logger.debug("Mock stock release: variantId={}, quantity={} - no-op", variantId, quantity)
    }

    override fun commitStock(
        variantId: Long,
        quantity: Int,
    ): Boolean {
        logger.debug("Mock stock commit: variantId={}, quantity={} - returning true", variantId, quantity)
        return quantity <= MOCK_STOCK_LEVEL
    }

    companion object {
        private const val MOCK_STOCK_LEVEL = 100
    }
}
