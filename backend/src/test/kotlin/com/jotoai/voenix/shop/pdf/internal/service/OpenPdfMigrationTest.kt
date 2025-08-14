package com.jotoai.voenix.shop.pdf.internal.service

import com.lowagie.text.Document
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfWriter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.ByteArrayOutputStream

/**
 * Basic test to verify OpenPDF migration is working correctly.
 * This test ensures that the OpenPDF library is properly integrated
 * and can perform basic PDF generation operations.
 */
class OpenPdfMigrationTest {

    @Test
    fun `should create simple PDF with OpenPDF library`() {
        val outputStream = ByteArrayOutputStream()
        val document = Document(Rectangle(595f, 842f)) // A4 size
        val writer = PdfWriter.getInstance(document, outputStream)
        
        document.open()
        val contentByte = writer.directContent
        
        // Test font creation
        val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        
        // Test text rendering
        contentByte.beginText()
        contentByte.setFontAndSize(baseFont, 12f)
        contentByte.moveText(100f, 700f)
        contentByte.showText("OpenPDF Migration Test - Success!")
        contentByte.endText()
        
        document.close()
        
        val pdfBytes = outputStream.toByteArray()
        
        // Basic validation - PDF should contain data and start with PDF header
        assertTrue(pdfBytes.isNotEmpty(), "PDF bytes should not be empty")
        assertTrue(pdfBytes.size > 100, "PDF should contain meaningful content")
        
        // Check PDF header
        val header = String(pdfBytes.take(8).toByteArray())
        assertTrue(header.startsWith("%PDF"), "PDF should start with proper header")
    }
    
    @Test
    fun `should create PDF with rotated text using OpenPDF library`() {
        val outputStream = ByteArrayOutputStream()
        val document = Document(Rectangle(200f, 200f))
        val writer = PdfWriter.getInstance(document, outputStream)
        
        document.open()
        val contentByte = writer.directContent
        
        // Test rotated text (key feature used in PDF generation)
        val baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        
        contentByte.beginText()
        contentByte.setFontAndSize(baseFont, 10f)
        // 90 degree rotation like in OrderPdfServiceImpl
        contentByte.setTextMatrix(0f, 1f, -1f, 0f, 50f, 150f)
        contentByte.showText("Rotated Text Test")
        contentByte.endText()
        
        document.close()
        
        val pdfBytes = outputStream.toByteArray()
        
        // Basic validation
        assertTrue(pdfBytes.isNotEmpty(), "PDF with rotated text should not be empty")
        assertTrue(pdfBytes.size > 200, "PDF with rotated text should contain data")
        
        val header = String(pdfBytes.take(8).toByteArray())
        assertTrue(header.startsWith("%PDF"), "PDF should start with proper header")
    }
}