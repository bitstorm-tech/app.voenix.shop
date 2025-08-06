package com.jotoai.voenix.shop.pdf

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

/**
 * Test to verify the PDF module structure and boundaries using Spring Modulith.
 */
class PdfModulithTest {
    private val modules = ApplicationModules.of("com.jotoai.voenix.shop")

    @Test
    fun `should list all modules`() {
        // Print all modules to see what's detected
        println("All modules:")
        modules.forEach { module ->
            println("  - ${module.name} (${module.displayName})")
            println("    Named interfaces: ${module.namedInterfaces.map { it.name }}")
        }
    }

    @Test
    fun `should generate module documentation`() {
        // Generate PlantUML documentation for all modules
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }

    @Test
    fun `should verify PDF module exists`() {
        // Check if PDF module exists
        val pdfModuleExists = modules.any { it.name == "pdf" }
        assert(pdfModuleExists) { "PDF module should exist" }

        if (pdfModuleExists) {
            val pdfModule = modules.getModuleByName("pdf").get()
            println("PDF module found: ${pdfModule.displayName}")
            println("Named interfaces: ${pdfModule.namedInterfaces.map { it.name }}")
        }
    }
}
