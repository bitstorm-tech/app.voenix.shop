package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Implementation of ImageFacade that delegates to internal services.
 */
@Service
class ImageFacadeImpl(
    private val imageService: ImageService,
    private val userImageStorageService: UserImageStorageService,
) : ImageFacade {
    
    override fun createUploadedImage(file: MultipartFile, userId: Long): ImageDto {
        val uploadedImage = userImageStorageService.storeUploadedImage(file, userId)
        return ImageDto(
            filename = uploadedImage.storedFilename,
            imageType = com.jotoai.voenix.shop.image.api.dto.ImageType.PRIVATE
        )
    }

    override fun createImage(request: CreateImageRequest): ImageDto =
        throw UnsupportedOperationException("Use store method with MultipartFile")

    override fun getUploadedImageByUuid(uuid: UUID, userId: Long): ImageDto {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun deleteUploadedImage(uuid: UUID, userId: Long) {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun getUserUploadedImages(userId: Long): List<ImageDto> {
        throw UnsupportedOperationException("Not yet implemented")
    }
}