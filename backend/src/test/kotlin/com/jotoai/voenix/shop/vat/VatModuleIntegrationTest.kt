package com.jotoai.voenix.shop.vat

import com.jotoai.voenix.shop.vat.api.VatFacade
import com.jotoai.voenix.shop.vat.api.VatQueryService
import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class VatModuleIntegrationTest {
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
}
