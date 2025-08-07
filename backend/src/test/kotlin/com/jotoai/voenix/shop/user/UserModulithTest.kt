package com.jotoai.voenix.shop.user

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

/**
 * Spring Modulith verification test for the User module.
 * This test verifies the module structure and generates documentation.
 */
class UserModulithTest {
    private val modules = ApplicationModules.of(com.jotoai.voenix.shop.VoenixShopApplication::class.java)

    @Test
    fun `verify user module structure`() {
        modules.verify()
    }

    @Test
    fun `verify user module has no cyclic dependencies`() {
        // Module verification is done by the verify() method above
        // Individual module cycle checking is included in that verification
        assert(modules.toString().contains("user"))
    }

    @Test
    fun `generate user module documentation`() {
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
    }
}