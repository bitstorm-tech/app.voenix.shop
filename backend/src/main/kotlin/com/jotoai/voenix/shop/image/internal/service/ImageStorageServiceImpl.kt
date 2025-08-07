package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Implementation of ImageStorageService that delegates to existing storage services.
 */
@Service
class ImageStorageServiceImpl(
    private val storagePathService: com.jotoai.voenix.shop.image.api.StoragePathService,
    private val imageService: ImageService,
) : ImageStorageService {
    override fun storeFile(
        file: MultipartFile,
        imageType: ImageType,
    ): String {
        // Delegate to the existing ImageService which handles storage
        throw UnsupportedOperationException("Use ImageFacade.createImage instead")
    }

    override fun storeFile(
        bytes: ByteArray,
        originalFilename: String,
        imageType: ImageType,
    ): String = throw UnsupportedOperationException("Not yet implemented")

    override fun loadFileAsResource(
        filename: String,
        imageType: ImageType,
    ): Resource = throw UnsupportedOperationException("Use ImageAccessService instead")

    override fun generateImageUrl(
        filename: String,
        imageType: ImageType,
    ): String = storagePathService.getImageUrl(imageType, filename)

    override fun deleteFile(
        filename: String,
        imageType: ImageType,
    ): Boolean {
        imageService.delete(filename)
        return true
    }

    override fun fileExists(
        filename: String,
        imageType: ImageType,
    ): Boolean = throw UnsupportedOperationException("Not yet implemented")
}
