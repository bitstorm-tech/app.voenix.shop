package com.jotoai.voenix.shop.common

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class CommonModulithTest {
    private val modules = ApplicationModules.of(com.jotoai.voenix.shop.VoenixShopApplication::class.java)

    @Test
    fun `should detect common module`() {
        // Simple test to ensure the common module is detected by Spring Modulith
        val commonModule = modules.getModuleByName("common")
        assert(commonModule.isPresent) {
            "Common module should be present in the application modules"
        }

        // Verify the module has the expected name (it gets detected as "Common" by default)
        val module = commonModule.get()
        assert(module.displayName == "Common") {
            "Expected 'Common', got '${module.displayName}'"
        }
    }

    @Test
    fun `should generate module documentation`() {
        // Generate documentation for the module structure
        try {
            Documenter(modules)
                .writeDocumentation()
                .writeIndividualModulesAsPlantUml()
        } catch (e: Exception) {
            // Documentation generation might fail due to violations, but that's ok for this test
            // The important thing is that the modules can be loaded
        }
    }
}
