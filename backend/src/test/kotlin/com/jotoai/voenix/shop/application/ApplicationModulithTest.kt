package com.jotoai.voenix.shop.application

import com.jotoai.voenix.shop.VoenixShopApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ApplicationModulithTest {
    private val modules = ApplicationModules.of(VoenixShopApplication::class.java)

    @Test
    fun verifiesModularStructure() {
        modules.verify()
    }

    @Test
    fun `should generate module documentation`() {
        Documenter(modules)
            .writeIndividualModulesAsPlantUml()
            .writeModulesAsPlantUml()
            .writeDocumentation()
    }
}
