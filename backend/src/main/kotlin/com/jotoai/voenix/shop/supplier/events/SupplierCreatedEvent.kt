package com.jotoai.voenix.shop.supplier.events

import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new supplier is created.
 */
@Externalized
data class SupplierCreatedEvent(
    val supplier: SupplierDto,
)
