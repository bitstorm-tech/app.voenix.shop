package com.jotoai.voenix.shop.vat

import com.jotoai.voenix.shop.vat.events.DefaultVatChangedEvent
import com.jotoai.voenix.shop.vat.events.VatCreatedEvent
import com.jotoai.voenix.shop.vat.events.VatDeletedEvent
import com.jotoai.voenix.shop.vat.events.VatUpdatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TestEventCapture {
    val capturedEvents = mutableListOf<Any>()

    fun clear() {
        capturedEvents.clear()
    }

    @EventListener
    fun handleVatCreatedEvent(event: VatCreatedEvent) {
        capturedEvents.add(event)
    }

    @EventListener
    fun handleVatUpdatedEvent(event: VatUpdatedEvent) {
        capturedEvents.add(event)
    }

    @EventListener
    fun handleVatDeletedEvent(event: VatDeletedEvent) {
        capturedEvents.add(event)
    }

    @EventListener
    fun handleDefaultVatChangedEvent(event: DefaultVatChangedEvent) {
        capturedEvents.add(event)
    }
}
