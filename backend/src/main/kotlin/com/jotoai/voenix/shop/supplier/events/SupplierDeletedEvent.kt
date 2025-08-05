package com.jotoai.voenix.shop.supplier.events

import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a supplier is deleted.
 */
@Externalized
data class SupplierDeletedEvent(
    val supplier: SupplierDto,
)
