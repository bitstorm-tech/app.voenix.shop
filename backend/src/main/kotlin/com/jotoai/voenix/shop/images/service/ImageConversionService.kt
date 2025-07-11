package com.jotoai.voenix.shop.images.service

import com.jotoai.voenix.shop.images.dto.CropArea
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.webp.WebpWriter
import org.springframework.stereotype.Service
import java.awt.Rectangle

@Service
class ImageConversionService {
    fun convertToWebP(imageBytes: ByteArray): ByteArray {
        val image = ImmutableImage.loader().fromBytes(imageBytes)

        val writer =
            WebpWriter()
                .withQ(90) // Quality setting (0-100, 90 is high quality)

        return image.bytes(writer)
    }

    fun cropImage(
        imageBytes: ByteArray,
        cropArea: CropArea,
    ): ByteArray {
        val image = ImmutableImage.loader().fromBytes(imageBytes)

        // Validate crop area bounds
        require(cropArea.x >= 0 && cropArea.y >= 0) {
            "Crop coordinates must be non-negative"
        }
        require(cropArea.x + cropArea.width <= image.width) {
            "Crop area exceeds image width"
        }
        require(cropArea.y + cropArea.height <= image.height) {
            "Crop area exceeds image height"
        }

        // Create rectangle for cropping
        val rectangle =
            Rectangle(
                cropArea.x,
                cropArea.y,
                cropArea.width,
                cropArea.height,
            )

        // Crop the image and return bytes in PNG format to maintain quality
        val croppedImage = image.subimage(rectangle)
        // Use PNG writer to maintain image quality without compression
        val writer: ImageWriter = PngWriter.MaxCompression
        return croppedImage.bytes(writer)
    }

    fun isWebPSupported(): Boolean = true
}
