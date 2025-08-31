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
        val cropRect = cropArea.toRectangle()

        return if (image.contains(cropRect)) {
            // Simple case: crop area is within bounds
            image.subimage(cropRect).toPngBytes()
        } else {
            // Complex case: create canvas and overlay visible portion
            createCanvasWithVisiblePortion(image, cropArea).toPngBytes()
        }
    }

    private fun CropArea.toRectangle() = Rectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())

    private fun ImmutableImage.contains(rect: Rectangle): Boolean =
        rect.x >= 0 &&
            rect.y >= 0 &&
            rect.x + rect.width <= width &&
            rect.y + rect.height <= height

    private fun ImmutableImage.toPngBytes(): ByteArray {
        val writer: ImageWriter = PngWriter.MaxCompression
        return bytes(writer)
    }

    private fun createCanvasWithVisiblePortion(
        image: ImmutableImage,
        cropArea: CropArea,
    ): ImmutableImage {
        val cropRect = cropArea.toRectangle()

        // Calculate intersection between crop area and image bounds
        val visibleRect = calculateVisibleRectangle(image, cropRect)

        // If no intersection, return transparent canvas
        if (visibleRect.width <= 0 || visibleRect.height <= 0) {
            return ImmutableImage.create(cropRect.width, cropRect.height)
        }

        // Create canvas and overlay visible portion
        val canvas = ImmutableImage.create(cropRect.width, cropRect.height)
        val visiblePortion = image.subimage(visibleRect)
        val overlayX = visibleRect.x - cropRect.x
        val overlayY = visibleRect.y - cropRect.y

        return canvas.overlay(visiblePortion, overlayX, overlayY)
    }

    private fun calculateVisibleRectangle(
        image: ImmutableImage,
        cropRect: Rectangle,
    ): Rectangle {
        val x = maxOf(0, cropRect.x)
        val y = maxOf(0, cropRect.y)
        val maxX = minOf(image.width, cropRect.x + cropRect.width)
        val maxY = minOf(image.height, cropRect.y + cropRect.height)
        val width = maxX - x
        val height = maxY - y

        return Rectangle(x, y, maxOf(0, width), maxOf(0, height))
    }
}
