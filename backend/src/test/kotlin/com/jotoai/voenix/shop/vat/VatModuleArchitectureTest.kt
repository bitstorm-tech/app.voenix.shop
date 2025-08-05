package com.jotoai.voenix.shop.vat

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class VatModuleArchitectureTest {
    private val modules = ApplicationModules.of("com.jotoai.voenix.shop")

    @Test
    fun `should verify module structure`() {
        // This test verifies that the module structure is valid
        // and that modules only expose their intended public API
        modules.verify()
    }

    @Test
    fun `should document module structure`() {
        // This test generates documentation for the module architecture
        // The documentation will be generated in the target/spring-modulith-docs directory
        Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml()
    }

    @Test
    fun `should verify VAT module exists`() {
        // Spring Modulith detects modules based on direct sub-packages of the application package
        // Our VAT module is now at com.jotoai.voenix.shop.vat
        val vatModule =
            modules
                .getModuleByName("vat")
                .orElseThrow { AssertionError("VAT module not found. Available modules: ${modules.map { it.name }}") }

        // Verify that the module exists and has the correct name
        assert(vatModule.name == "vat") {
            "Expected module name to be 'vat', but was '${vatModule.name}'"
        }
    }

    @Test
    fun `should verify no cyclic dependencies between modules`() {
        // This is a basic check that modules.verify() passes,
        // which includes checking for cyclic dependencies
        try {
            modules.verify()
        } catch (e: Exception) {
            throw AssertionError("Module verification failed, possibly due to cyclic dependencies: ${e.message}")
        }
    }
}
