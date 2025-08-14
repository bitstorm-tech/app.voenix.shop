package com.jotoai.voenix.shop.vat

import com.jotoai.voenix.shop.vat.api.VatService
import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VatModuleIntegrationTest {
    @Autowired
    private lateinit var vatService: VatService

    @Test
    fun `should expose correct public interfaces`() {
        // Given - initial state
        val initialVats = vatService.getAllVats()

        // When - creating a new VAT
        val createRequest =
            CreateValueAddedTaxRequest(
                name = "Test VAT",
                percent = 20,
                description = "Test VAT for integration test",
                isDefault = true,
            )

        val createdVat = vatService.createVat(createRequest)

        // Then - verify creation
        assertThat(createdVat.name).isEqualTo("Test VAT")
        assertThat(createdVat.percent).isEqualTo(20)
        assertThat(createdVat.isDefault).isTrue()

        // And - verify query operations
        val allVats = vatService.getAllVats()
        assertThat(allVats).hasSize(initialVats.size + 1)

        val retrievedVat = vatService.getVatById(createdVat.id)
        assertThat(retrievedVat).isEqualTo(createdVat)

        val defaultVat = vatService.getDefaultVat()
        assertThat(defaultVat).isEqualTo(createdVat)

        // And - verify service operations
        assertThat(vatService.getVatById(createdVat.id)).isEqualTo(createdVat)
        assertThat(vatService.getDefaultVat()).isEqualTo(createdVat)
        assertThat(vatService.existsById(createdVat.id)).isTrue()

        // When - deleting the VAT
        vatService.deleteVat(createdVat.id)

        // Then - verify deletion
        val finalVats = vatService.getAllVats()
        assertThat(finalVats).hasSize(initialVats.size)
        assertThat(vatService.existsById(createdVat.id)).isFalse()
    }
}
