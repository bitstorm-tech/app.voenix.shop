package com.jotoai.voenix.shop.supplier

import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.supplier.SupplierService
import com.jotoai.voenix.shop.supplier.CreateSupplierRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Transactional
class SupplierModuleIntegrationTest {
    @Autowired
    private lateinit var supplierService: SupplierService

    @Test
    fun `should expose correct public interfaces`() {
        // Given - initial state
        val initialSuppliers = supplierService.getAllSuppliers()

        // When - creating a new supplier
        val createRequest =
            CreateSupplierRequest(
                name = "Test Supplier Ltd.",
                title = "CEO",
                firstName = "John",
                lastName = "Doe",
                street = "Main Street",
                houseNumber = "123",
                city = "Test City",
                postalCode = 12345,
                countryId = null,
                phoneNumber1 = "+49 123 456 789",
                phoneNumber2 = null,
                phoneNumber3 = null,
                email = "test@testsupplier.com",
                website = "https://testsupplier.com",
            )

        val createdSupplier = supplierService.createSupplier(createRequest)

        // Then - verify creation
        assertThat(createdSupplier.name).isEqualTo("Test Supplier Ltd.")
        assertThat(createdSupplier.firstName).isEqualTo("John")
        assertThat(createdSupplier.lastName).isEqualTo("Doe")
        assertThat(createdSupplier.email).isEqualTo("test@testsupplier.com")
        assertThat(createdSupplier.website).isEqualTo("https://testsupplier.com")

        // And - verify query operations
        val allSuppliers = supplierService.getAllSuppliers()
        assertThat(allSuppliers).hasSize(initialSuppliers.size + 1)

        val retrievedSupplier = supplierService.getSupplierById(createdSupplier.id)
        assertThat(retrievedSupplier).isEqualTo(createdSupplier)

        // And - verify SPI operations
        assertThat(supplierService.getSupplierById(createdSupplier.id)).isEqualTo(createdSupplier)
        assertThat(supplierService.existsById(createdSupplier.id)).isTrue()

        // When - deleting the supplier
        supplierService.deleteSupplier(createdSupplier.id)

        // Then - verify deletion
        val finalSuppliers = supplierService.getAllSuppliers()
        assertThat(finalSuppliers).hasSize(initialSuppliers.size)
        assertThat(supplierService.existsById(createdSupplier.id)).isFalse()
    }

    @Test
    fun `should validate unique constraints`() {
        // Given - a supplier
        val createRequest =
            CreateSupplierRequest(
                name = "Unique Test Supplier",
                title = null,
                firstName = "Test",
                lastName = "User",
                street = null,
                houseNumber = null,
                city = null,
                postalCode = null,
                countryId = null,
                phoneNumber1 = null,
                phoneNumber2 = null,
                phoneNumber3 = null,
                email = "unique@test.com",
                website = null,
            )

        val createdSupplier = supplierService.createSupplier(createRequest)

        // When - trying to create another supplier with same name
        val duplicateNameRequest = createRequest.copy(email = "different@test.com")

        // Then - should throw exception
        try {
            supplierService.createSupplier(duplicateNameRequest)
            throw AssertionError("Should have thrown ResourceAlreadyExistsException for duplicate name")
        } catch (e: ResourceAlreadyExistsException) {
            assertThat(e.message).contains("Supplier already exists with name")
        }

        // When - trying to create another supplier with same email
        val duplicateEmailRequest = createRequest.copy(name = "Different Name")

        // Then - should throw exception
        try {
            supplierService.createSupplier(duplicateEmailRequest)
            throw AssertionError("Should have thrown ResourceAlreadyExistsException for duplicate email")
        } catch (e: ResourceAlreadyExistsException) {
            assertThat(e.message).contains("Supplier already exists with email")
        }

        // Cleanup
        supplierService.deleteSupplier(createdSupplier.id)
    }
}
