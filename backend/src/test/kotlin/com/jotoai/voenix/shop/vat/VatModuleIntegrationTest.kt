package com.jotoai.voenix.shop.vat

import com.jotoai.voenix.shop.vat.api.VatService
import com.jotoai.voenix.shop.vat.internal.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.internal.service.VatServiceImpl
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

    @Autowired
    private lateinit var vatServiceImpl: VatServiceImpl

    @Test
    fun `should expose correct public interfaces and internal functions`() {
        // Given - initial state (internal DTOs via internal service)
        val initialVats = vatServiceImpl.getAllVats()

        // When - creating a new VAT (internal request/DTO)
        val createRequest =
            CreateValueAddedTaxRequest(
                name = "Test VAT",
                percent = 20,
                description = "Test VAT for integration test",
                isDefault = true,
            )

        val createdVat = vatServiceImpl.createVat(createRequest)

        // Then - verify creation
        assertThat(createdVat.name).isEqualTo("Test VAT")
        assertThat(createdVat.percent).isEqualTo(20)
        assertThat(createdVat.isDefault).isTrue()

        // And - verify query operations (internal)
        val allVats = vatServiceImpl.getAllVats()
        assertThat(allVats).hasSize(initialVats.size + 1)

        val retrievedVat = vatServiceImpl.getVatById(createdVat.id)
        assertThat(retrievedVat).isEqualTo(createdVat)

        // And - verify public interface is minimal but functional cross-module
        assertThat(vatService.existsById(createdVat.id)).isTrue()

        // When - deleting the VAT (internal)
        vatServiceImpl.deleteVat(createdVat.id)

        // Then - verify deletion
        val finalVats = vatServiceImpl.getAllVats()
        assertThat(finalVats).hasSize(initialVats.size)
        assertThat(vatService.existsById(createdVat.id)).isFalse()
    }
}
