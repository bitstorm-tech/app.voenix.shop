/**
 * Domain events published by the supplier module.
 * <p>
 * This package contains domain events that are published when supplier-related
 * operations occur. Other modules can listen to these events for reactive integration.
 * <ul>
 *   <li>SupplierCreatedEvent - Published when a new supplier is created</li>
 *   <li>SupplierUpdatedEvent - Published when a supplier is updated</li>
 *   <li>SupplierDeletedEvent - Published when a supplier is deleted</li>
 * </ul>
 */
@org.springframework.modulith.NamedInterface("events")
package com.jotoai.voenix.shop.supplier.events;