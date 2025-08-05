package com.jotoai.voenix.shop.modules.vat

import com.jotoai.voenix.shop.modules.vat.api.VatFacade
import com.jotoai.voenix.shop.modules.vat.api.VatQueryService
import com.jotoai.voenix.shop.modules.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.modules.vat.events.DefaultVatChangedEvent
import com.jotoai.voenix.shop.modules.vat.events.VatCreatedEvent
import com.jotoai.voenix.shop.modules.vat.events.VatDeletedEvent
import com.jotoai.voenix.shop.modules.vat.events.VatUpdatedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.event.EventListener
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class VatModuleIntegrationTest {
    private val capturedEvents = mutableListOf<Any>()

    @Autowired
    private lateinit var vatFacade: VatFacade

    @Autowired
    private lateinit var vatQueryService: VatQueryService

    @Test
    fun `should expose correct public interfaces`() {
        // Given - initial state
        val initialVats = vatQueryService.getAllVats()

        // When - creating a new VAT
        val createRequest =
            CreateValueAddedTaxRequest(
                name = "Test VAT",
                percent = 20,
                description = "Test VAT for integration test",
                isDefault = true,
            )

        val createdVat = vatFacade.createVat(createRequest)

        // Then - verify creation
        assertThat(createdVat.name).isEqualTo("Test VAT")
        assertThat(createdVat.percent).isEqualTo(20)
        assertThat(createdVat.isDefault).isTrue()

        // And - verify query operations
        val allVats = vatQueryService.getAllVats()
        assertThat(allVats).hasSize(initialVats.size + 1)

        val retrievedVat = vatQueryService.getVatById(createdVat.id)
        assertThat(retrievedVat).isEqualTo(createdVat)

        val defaultVat = vatQueryService.getDefaultVat()
        assertThat(defaultVat).isEqualTo(createdVat)

        // And - verify SPI operations
        assertThat(vatQueryService.getVatById(createdVat.id)).isEqualTo(createdVat)
        assertThat(vatQueryService.getDefaultVat()).isEqualTo(createdVat)
        assertThat(vatQueryService.existsById(createdVat.id)).isTrue()

        // When - deleting the VAT
        vatFacade.deleteVat(createdVat.id)

        // Then - verify deletion
        val finalVats = vatQueryService.getAllVats()
        assertThat(finalVats).hasSize(initialVats.size)
        assertThat(vatQueryService.existsById(createdVat.id)).isFalse()
    }

    @Test
    fun `should publish domain events correctly`() {
        // Given - clear captured events
        capturedEvents.clear()

        // When - creating a VAT
        val createRequest =
            CreateValueAddedTaxRequest(
                name = "Event Test VAT",
                percent = 15,
                description = "Test VAT for event testing",
                isDefault = true,
            )

        val createdVat = vatFacade.createVat(createRequest)

        // Then - verify VatCreatedEvent was published
        val createdEvents = capturedEvents.filterIsInstance<VatCreatedEvent>()
        assertThat(createdEvents).hasSize(1)
        assertThat(createdEvents.first().vat.id).isEqualTo(createdVat.id)

        // And - verify DefaultVatChangedEvent was published
        val defaultChangedEvents = capturedEvents.filterIsInstance<DefaultVatChangedEvent>()
        assertThat(defaultChangedEvents).hasSize(1)
        assertThat(defaultChangedEvents.first().newDefaultId).isEqualTo(createdVat.id)

        // When - deleting the VAT
        capturedEvents.clear()
        vatFacade.deleteVat(createdVat.id)

        // Then - verify VatDeletedEvent was published
        val deletedEvents = capturedEvents.filterIsInstance<VatDeletedEvent>()
        assertThat(deletedEvents).hasSize(1)
        assertThat(deletedEvents.first().vat.id).isEqualTo(createdVat.id)

        // And - verify DefaultVatChangedEvent was published for deletion
        val deletedDefaultChangedEvents = capturedEvents.filterIsInstance<DefaultVatChangedEvent>()
        assertThat(deletedDefaultChangedEvents).hasSize(1)
        assertThat(deletedDefaultChangedEvents.first().previousDefaultId).isEqualTo(createdVat.id)
        assertThat(deletedDefaultChangedEvents.first().newDefaultId).isNull()
    }

    // Event listeners to capture events for testing
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
