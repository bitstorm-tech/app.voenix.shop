/**
 * Public API for the VAT module.
 * <p>
 * This package contains the unified service interface and DTOs that other modules
 * can use to interact with the VAT module. The API is exposed as a named
 * interface "api" to ensure explicit dependencies.
 * </p>
 * <p>
 * The main entry point is {@link com.jotoai.voenix.shop.vat.api.VatService} which provides
 * both command and query operations for VAT management.
 * </p>
 */
@org.springframework.modulith.NamedInterface("api")
package com.jotoai.voenix.shop.vat.api;