/**
 * Article Web Layer
 * 
 * This package contains REST controllers that serve as web adapters for the Article module.
 * These controllers expose HTTP endpoints for both public and admin article operations.
 * 
 * Controllers in this package:
 * - PublicMugController: Public API for retrieving mug articles for customers
 * - AdminArticleController: Admin operations for article management
 * - AdminMugVariantController: Admin operations for mug variant management
 * - AdminShirtVariantController: Admin operations for shirt variant management
 * - AdminArticleCategoryController: Admin operations for article category management
 * - AdminArticleSubCategoryController: Admin operations for article sub-category management
 * 
 * All controllers follow Spring Modulith principles by:
 * - Only accessing services from the same module (article::api)
 * - Using DTOs defined in the module's public API
 * - Proper error handling through module-specific exception handlers
 * 
 * @see com.jotoai.voenix.shop.article.api for public module interfaces
 * @see com.jotoai.voenix.shop.article.internal.exception.ArticleExceptionHandler for error handling
 */
package com.jotoai.voenix.shop.article.web;