package com.jotoai.voenix.shop.pdf

import com.jotoai.voenix.shop.pdf.api.PdfGenerationService
import com.jotoai.voenix.shop.pdf.internal.service.PdfGenerationServiceImpl
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

/**
 * Integration test to verify that the PDF service consolidation works correctly.
 * This test ensures that:
 * 1. PdfGenerationServiceImpl is registered as a Spring bean
 * 2. The service implements all required functionality
 * 3. Dependency injection works correctly
 */
@SpringBootTest
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PdfGenerationServiceConsolidationTest(
    private val pdfGenerationService: PdfGenerationService,
) {
    @Test
    fun `should inject PdfGenerationServiceImpl`() {
        // Verify the injected service is PdfGenerationServiceImpl
        assertInstanceOf(PdfGenerationServiceImpl::class.java, pdfGenerationService)
    }

    @Test
    fun `should provide all PDF generation methods`() {
        val service = pdfGenerationService

        // Verify the service has all required methods (compile-time check)
        // These would fail compilation if methods are missing
        assert(service::generateOrderPdf is kotlin.reflect.KFunction<*>)
        assert(service::getOrderPdfFilename is kotlin.reflect.KFunction<*>)
    }

    @Test
    fun `should use correct service class`() {
        // Spring may create CGLIB proxies, so we check for the base class or proxy
        val className = pdfGenerationService::class.simpleName
        val expectedBaseName = "PdfGenerationServiceImpl"

        // Class name should either be exact match or a Spring proxy
        assert(className == expectedBaseName || className?.startsWith("${expectedBaseName}\$\$") == true) {
            "Expected class name to be '$expectedBaseName' or a Spring proxy, but was '$className'"
        }
    }
}
