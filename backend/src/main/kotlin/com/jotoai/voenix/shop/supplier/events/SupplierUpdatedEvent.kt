package com.jotoai.voenix.shop.supplier.events

import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a supplier is updated.
 */
@Externalized
data class SupplierUpdatedEvent(
    val oldSupplier: SupplierDto,
    val newSupplier: SupplierDto,
)
