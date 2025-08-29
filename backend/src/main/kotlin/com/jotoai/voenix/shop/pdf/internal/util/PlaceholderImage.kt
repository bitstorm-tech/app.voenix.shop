package com.jotoai.voenix.shop.pdf.internal.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Utility object for handling placeholder images in PDF generation.
 * Uses a static approach to avoid runtime image generation overhead.
 */
object PlaceholderImage {
    private val logger = KotlinLogging.logger {}

    private const val WIDTH = 400
    private const val HEIGHT = 300
    private const val FONT_SIZE = 24

    /**
     * Gets the default placeholder image bytes.
     * Falls back to runtime generation if static resource is not available.
     */
    val DEFAULT_BYTES: ByteArray by lazy {
        // Try to load static resource first
        try {
            PlaceholderImage::class.java.getResourceAsStream("/static/images/placeholder.png")?.use { stream ->
                stream.readBytes()
            } ?: generatePlaceholderImage()
        } catch (e: IOException) {
            logger.warn(e) { "Could not load static placeholder image, generating runtime placeholder" }
            generatePlaceholderImage()
        }
    }

    /**
     * Generates a simple placeholder image at runtime as fallback.
     * This is only used if the static resource is not available.
     */
    private fun generatePlaceholderImage(): ByteArray {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        try {
            // Fill with light gray background
            graphics.color = Color.LIGHT_GRAY
            graphics.fillRect(0, 0, WIDTH, HEIGHT)

            // Add border
            graphics.color = Color.DARK_GRAY
            graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1)

            // Add placeholder text
            graphics.color = Color.BLACK
            graphics.font = Font("Arial", Font.BOLD, FONT_SIZE)
            val text = "No Image Available"
            val fontMetrics = graphics.fontMetrics
            val textWidth = fontMetrics.stringWidth(text)
            val textHeight = fontMetrics.ascent
            val x = (WIDTH - textWidth) / 2
            val y = (HEIGHT + textHeight) / 2
            graphics.drawString(text, x, y)

            // Convert to byte array
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", outputStream)
            return outputStream.toByteArray()
        } finally {
            graphics.dispose()
        }
    }
}
