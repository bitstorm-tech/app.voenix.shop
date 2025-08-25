package com.jotoai.voenix.shop.openai

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["app.test-mode=true"])
class OpenAIModulithTest {
    @Test
    fun `should verify OpenAI module structure`() {
        val modules = ApplicationModules.of(com.jotoai.voenix.shop.VoenixShopApplication::class.java)

        modules.verify()

        val openaiModule = modules.getModuleByName("openai")
        assert(openaiModule.isPresent) { "OpenAI module should be present" }
    }
}
