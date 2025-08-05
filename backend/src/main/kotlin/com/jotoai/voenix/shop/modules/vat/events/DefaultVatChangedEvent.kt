package com.jotoai.voenix.shop.modules.vat.events

import org.springframework.modulith.events.Externalized

/**
 * Event published when the default VAT configuration changes.
 * This can happen when:
 * - A new VAT is set as default (previousDefaultId is set, newDefaultId is set)
 * - An existing default VAT is updated to non-default (previousDefaultId is set, newDefaultId is null)
 * - A default VAT is deleted (previousDefaultId is set, newDefaultId is null)
 */
@Externalized
data class DefaultVatChangedEvent(
    val previousDefaultId: Long?,
    val newDefaultId: Long?,
)
