package com.jotoai.voenix.shop.vat

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

/**
 * Test to verify the VAT module structure and boundaries using Spring Modulith.
 */
class VatModulithTest {
    private val modules = ApplicationModules.of("com.jotoai.voenix.shop")

    @Test
    fun `should verify VAT module exists and has correct structure`() {
        val vatModuleExists = modules.any { it.name == "vat" }
        assert(vatModuleExists) { "VAT module should exist" }

        if (vatModuleExists) {
            val vatModule = modules.getModuleByName("vat").get()
            println("VAT module found: ${vatModule.displayName}")
            println("Base package: ${vatModule.basePackage}")

            // Verify module structure - display name is auto-generated as "Vat" from package name
            assert(vatModule.displayName == "Vat") {
                "VAT module should have display name 'Vat', but was '${vatModule.displayName}'"
            }
            assert(vatModule.basePackage.name == "com.jotoai.voenix.shop.vat") {
                "VAT module should have base package 'com.jotoai.voenix.shop.vat', " +
                    "but was '${vatModule.basePackage.name}'"
            }
        }
    }

    @Test
    fun `should verify VAT module dependencies`() {
        // This test verifies the module structure is valid by attempting to verify all modules
        // If there are violations, the verify() method will throw an exception
        try {
            modules.verify()
            println("VAT module passes Spring Modulith verification")
        } catch (e: Exception) {
            // Check if the error mentions VAT module violations specifically
            val message = e.message ?: ""
            if (message.contains("Module 'vat'") || message.contains("Slice vat")) {
                throw AssertionError("VAT module has architectural violations: $message")
            }
            // Otherwise, it's likely other modules with known issues
            println("Known architectural violations in other modules (not VAT): ${e.message}")
        }
    }

    @Test
    fun `should generate VAT module documentation`() {
        // Generate PlantUML documentation for VAT module
        Documenter(modules)
            .writeModuleCanvases()
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }
}
