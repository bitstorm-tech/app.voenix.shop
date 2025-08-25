package com.jotoai.voenix.shop.auth

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.test.ApplicationModuleTest

/**
 * Tests to verify the structural integrity of the auth module.
 *
 * This test ensures that:
 * - Module boundaries are respected
 * - Only the API interface is publicly accessible
 * - Internal components are properly encapsulated
 * - Dependencies are correctly declared
 */
class AuthModulithTest {
    @Test
    fun `should verify auth module structure`() {
        val modules = ApplicationModules.of("com.jotoai.voenix.shop")
        modules.verify()
    }
}

/**
 * Integration test for the auth module in isolation.
 *
 * This test bootstraps only the auth module and its direct dependencies,
 * ensuring it can operate independently and testing its boundaries.
 */
@ApplicationModuleTest
class AuthModuleIntegrationTest {
    @Test
    fun `should bootstrap auth module successfully`() {
        // This test verifies that the auth module can be started in isolation
        // and that all its components can be properly wired together
    }
}
