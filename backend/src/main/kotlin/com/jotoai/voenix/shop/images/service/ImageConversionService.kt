package com.jotoai.voenix.shop.images.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
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

    fun isWebPSupported(): Boolean = true
}
