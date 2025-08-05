package com.jotoai.voenix.shop.modules.vat.events

import com.jotoai.voenix.shop.modules.vat.api.dto.ValueAddedTaxDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new VAT configuration is created.
 */
@Externalized
data class VatCreatedEvent(
    val vat: ValueAddedTaxDto,
)
