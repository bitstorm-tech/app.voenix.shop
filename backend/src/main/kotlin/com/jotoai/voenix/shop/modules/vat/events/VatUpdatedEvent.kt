package com.jotoai.voenix.shop.modules.vat.events

import com.jotoai.voenix.shop.modules.vat.api.dto.ValueAddedTaxDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a VAT configuration is updated.
 */
@Externalized
data class VatUpdatedEvent(
    val oldVat: ValueAddedTaxDto,
    val newVat: ValueAddedTaxDto,
)
