package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter
import com.sksamuel.scrimage.webp.WebpWriter
import org.springframework.stereotype.Service
import java.awt.Rectangle

@Service
class ImageConversionService {
    companion object {
        private const val WEBP_QUALITY = 90 // Quality setting (0-100, high quality)
    }

    fun convertToWebP(imageBytes: ByteArray): ByteArray {
        val image = ImmutableImage.loader().fromBytes(imageBytes)

        val writer =
            WebpWriter()
                .withQ(WEBP_QUALITY)

        return image.bytes(writer)
    }

    fun convertToPng(imageBytes: ByteArray): ByteArray {
        val image = ImmutableImage.loader().fromBytes(imageBytes)
        val writer: ImageWriter = PngWriter.MaxCompression
        return image.bytes(writer)
    }

    fun cropImage(
        imageBytes: ByteArray,
        cropArea: CropArea,
    ): ByteArray {
        val image = ImmutableImage.loader().fromBytes(imageBytes)
        
        val cropX = cropArea.x.toInt()
        val cropY = cropArea.y.toInt()
        val cropWidth = cropArea.width.toInt()
        val cropHeight = cropArea.height.toInt()

        // Calculate the intersection between crop area and image bounds
        val sourceX = maxOf(0, cropX)
        val sourceY = maxOf(0, cropY)
        val sourceWidth = minOf(image.width - sourceX, cropWidth - (sourceX - cropX))
        val sourceHeight = minOf(image.height - sourceY, cropHeight - (sourceY - cropY))

        // If there's no intersection, return a transparent image
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            val transparentImage = ImmutableImage.create(cropWidth, cropHeight)
            val writer: ImageWriter = PngWriter.MaxCompression
            return transparentImage.bytes(writer)
        }

        // Calculate where to place the cropped portion on the result canvas
        val destX = sourceX - cropX
        val destY = sourceY - cropY

        // If the crop area is entirely within the image bounds, use simple cropping
        if (cropX >= 0 && cropY >= 0 && cropX + cropWidth <= image.width && cropY + cropHeight <= image.height) {
            val rectangle = Rectangle(cropX, cropY, cropWidth, cropHeight)
            val croppedImage = image.subimage(rectangle)
            val writer: ImageWriter = PngWriter.MaxCompression
            return croppedImage.bytes(writer)
        }

        // Create a transparent canvas for the final result
        val resultImage = ImmutableImage.create(cropWidth, cropHeight)
        
        // Extract the portion of the source image that intersects with the crop area
        val sourceRectangle = Rectangle(sourceX, sourceY, sourceWidth, sourceHeight)
        val sourcePortion = image.subimage(sourceRectangle)
        
        // Overlay the source portion onto the transparent canvas at the correct position
        val finalImage = resultImage.overlay(sourcePortion, destX, destY)
        
        // Use PNG writer to maintain transparency
        val writer: ImageWriter = PngWriter.MaxCompression
        return finalImage.bytes(writer)
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
