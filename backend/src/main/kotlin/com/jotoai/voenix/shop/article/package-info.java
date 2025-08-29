/**
 * Article Management Module
 * 
 * This module handles all aspects of article management in the Voenix Shop application.
 * It provides functionality for managing articles (mugs, shirts), their variants, categories,
 * cost calculations, and public article queries for the e-commerce frontend.
 * 
 * The module follows Spring Modulith architecture patterns with clear API boundaries:
 * - api/: Public interfaces, DTOs, and exceptions exposed to other modules
 * - internal/: Private implementation details including entities, repositories, and services
 * - web/: REST controllers and web-related components (Spring MVC adapters)
 * 
 * Key responsibilities:
 * - Article lifecycle management (creation, updates, retrieval)
 * - Article variant management (mugs and shirts with different sizes, colors, etc.)
 * - Article categorization and sub-categorization
 * - Cost calculation and pricing logic
 * - Public API for customer-facing article queries
 * 
 * Dependencies:
 * - application: Shared utilities and base classes
 * - vat::api: VAT calculation services for pricing
 * - supplier::api: Supplier information for articles
 * - user::api: User context for admin operations
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Article Management",
    allowedDependencies = {"application", "vat::api", "supplier::api", "user::api"}
)
package com.jotoai.voenix.shop.article;