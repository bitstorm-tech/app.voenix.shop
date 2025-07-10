package com.jotoai.voenix.shop.images.service

import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter

@Service
class ImageConversionService {
    fun convertToWebP(imageBytes: ByteArray): ByteArray {
        // Read the input image
        val inputStream = ByteArrayInputStream(imageBytes)
        val bufferedImage =
            ImageIO.read(inputStream)
                ?: throw IllegalArgumentException("Unable to read image")

        // Get WebP writer
        val writers = ImageIO.getImageWritersByFormatName("webp")
        if (!writers.hasNext()) {
            throw UnsupportedOperationException("WebP format not supported. Ensure webp-imageio dependency is included.")
        }
        val writer: ImageWriter = writers.next()

        // Set up output stream
        val outputStream = ByteArrayOutputStream()
        val imageOutputStream = ImageIO.createImageOutputStream(outputStream)
        writer.output = imageOutputStream

        // Configure write parameters for quality
        val writeParam = writer.defaultWriteParam
        if (writeParam.canWriteCompressed()) {
            writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            writeParam.compressionType = writeParam.compressionTypes[0]
            writeParam.compressionQuality = 0.9f // High quality
        }

        // Write the image
        try {
            val iioImage = IIOImage(bufferedImage, null, null)
            writer.write(null, iioImage, writeParam)
        } finally {
            writer.dispose()
            imageOutputStream.close()
        }

        return outputStream.toByteArray()
    }

    fun isWebPSupported(): Boolean = ImageIO.getImageWritersByFormatName("webp").hasNext()
}
