/**
 * REST API controllers for the Voenix Shop application.
 * <p>
 * This module contains all REST controllers that expose the application's
 * functionality through HTTP endpoints. It depends on various domain modules
 * to handle business logic.
 * </p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "REST API",
    allowedDependencies = {
            "vat::api",
            "vat::api.dto",
            "supplier::api",
            "supplier::api.dto",
            "auth",
            "domain",
            "common"
    }
)
package com.jotoai.voenix.shop.api;