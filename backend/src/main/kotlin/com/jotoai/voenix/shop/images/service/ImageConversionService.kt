package com.jotoai.voenix.shop.images.service

import com.jotoai.voenix.shop.images.dto.ConversionOptions
import com.jotoai.voenix.shop.images.dto.ConvertedImage
import com.jotoai.voenix.shop.images.dto.ImageFormat
import org.springframework.stereotype.Service
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

@Service
class ImageConversionService {
    fun convert(
        imageData: ByteArray,
        targetFormat: ImageFormat,
        options: ConversionOptions = ConversionOptions(),
    ): ConvertedImage {
        val inputStream = ByteArrayInputStream(imageData)
        val originalImage =
            ImageIO.read(inputStream)
                ?: throw IllegalArgumentException("Unable to read image data")

        val resizedImage =
            if (options.maxWidth != null || options.maxHeight != null) {
                resizeImage(originalImage, options.maxWidth, options.maxHeight)
            } else {
                originalImage
            }

        val outputStream = ByteArrayOutputStream()

        when (targetFormat) {
            ImageFormat.JPEG -> writeJpeg(resizedImage, outputStream, options.quality)
            ImageFormat.PNG -> writePng(resizedImage, outputStream)
            ImageFormat.WEBP -> writeWebp(resizedImage, outputStream, options.quality)
            ImageFormat.GIF -> writeGif(resizedImage, outputStream)
        }

        return ConvertedImage(
            data = outputStream.toByteArray(),
            format = targetFormat,
            width = resizedImage.width,
            height = resizedImage.height,
        )
    }

    fun detectFormat(imageData: ByteArray): ImageFormat? {
        val inputStream = ByteArrayInputStream(imageData)
        val imageInputStream = ImageIO.createImageInputStream(inputStream)
        val readers = ImageIO.getImageReaders(imageInputStream)

        if (!readers.hasNext()) {
            return null
        }

        val reader = readers.next()
        val formatName = reader.formatName.lowercase()

        return when (formatName) {
            "png" -> ImageFormat.PNG
            "jpeg", "jpg" -> ImageFormat.JPEG
            "webp" -> ImageFormat.WEBP
            "gif" -> ImageFormat.GIF
            else -> null
        }
    }

    private fun resizeImage(
        originalImage: BufferedImage,
        maxWidth: Int?,
        maxHeight: Int?,
    ): BufferedImage {
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        var newWidth = originalWidth
        var newHeight = originalHeight

        if (maxWidth != null && originalWidth > maxWidth) {
            newWidth = maxWidth
            newHeight = (originalHeight.toDouble() * maxWidth / originalWidth).toInt()
        }

        if (maxHeight != null && newHeight > maxHeight) {
            newHeight = maxHeight
            newWidth = (originalWidth.toDouble() * maxHeight / originalHeight).toInt()
        }

        if (newWidth == originalWidth && newHeight == originalHeight) {
            return originalImage
        }

        val resizedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null)
        graphics.dispose()

        return resizedImage
    }

    private fun writeJpeg(
        image: BufferedImage,
        outputStream: ByteArrayOutputStream,
        quality: Float,
    ) {
        val jpegImage =
            if (image.type == BufferedImage.TYPE_INT_ARGB || image.transparency != BufferedImage.OPAQUE) {
                val rgbImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
                val graphics = rgbImage.createGraphics()
                graphics.drawImage(image, 0, 0, null)
                graphics.dispose()
                rgbImage
            } else {
                image
            }

        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val writeParam = writer.defaultWriteParam
        writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
        writeParam.compressionQuality = quality

        val ios = ImageIO.createImageOutputStream(outputStream)
        writer.output = ios
        writer.write(null, IIOImage(jpegImage, null, null), writeParam)
        ios.close()
        writer.dispose()
    }

    private fun writePng(
        image: BufferedImage,
        outputStream: ByteArrayOutputStream,
    ) {
        ImageIO.write(image, "png", outputStream)
    }

    private fun writeWebp(
        image: BufferedImage,
        outputStream: ByteArrayOutputStream,
        quality: Float,
    ) {
        val writer = ImageIO.getImageWritersByFormatName("webp").next()
        val writeParam = writer.defaultWriteParam

        if (writeParam.canWriteCompressed()) {
            writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
            writeParam.compressionQuality = quality
        }

        val ios = ImageIO.createImageOutputStream(outputStream)
        writer.output = ios
        writer.write(null, IIOImage(image, null, null), writeParam)
        ios.close()
        writer.dispose()
    }

    private fun writeGif(
        image: BufferedImage,
        outputStream: ByteArrayOutputStream,
    ) {
        ImageIO.write(image, "gif", outputStream)
    }
}
