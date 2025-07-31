package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.domain.images.dto.CropArea
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.webp.WebpWriter
import java.awt.Rectangle
import org.springframework.stereotype.Service

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

        require(cropArea.x >= 0 && cropArea.y >= 0) {
            "Crop coordinates must be non-negative"
        }
        require(cropArea.x + cropArea.width <= image.width) {
            "Crop area exceeds image width"
        }
        require(cropArea.y + cropArea.height <= image.height) {
            "Crop area exceeds image height"
        }

        val rectangle =
            Rectangle(
                cropArea.x.toInt(),
                cropArea.y.toInt(),
                cropArea.width.toInt(),
                cropArea.height.toInt(),
            )

        // Crop the image and return bytes in PNG format to maintain quality
        val croppedImage = image.subimage(rectangle)
        // Use PNG writer to maintain image quality without compression
        val writer: ImageWriter = PngWriter.MaxCompression
        return croppedImage.bytes(writer)
    }

    fun getImageDimensions(imageBytes: ByteArray): ImageDimensions {
        val image = ImmutableImage.loader().fromBytes(imageBytes)
        return ImageDimensions(width = image.width, height = image.height)
    }

    data class ImageDimensions(
        val width: Int,
        val height: Int,
    )
}
