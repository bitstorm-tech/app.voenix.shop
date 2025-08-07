/**
 * Domain layer for the Voenix Shop application.
 * <p>
 * This module contains core business logic, entities, repositories, and services
 * that implement the domain model of the application.
 * </p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Domain",
    allowedDependencies = {"vat::api", "vat::api.dto", "auth", "common", "user::api"}
)
@org.springframework.modulith.ApplicationModule.Type(
    org.springframework.modulith.ApplicationModule.Type.Value.OPEN
)
package com.jotoai.voenix.shop.domain;