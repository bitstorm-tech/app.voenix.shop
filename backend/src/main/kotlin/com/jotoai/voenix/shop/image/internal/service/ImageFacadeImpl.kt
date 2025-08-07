package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Implementation of ImageFacade that delegates to internal services.
 */
@Service
class ImageFacadeImpl(
    private val imageService: ImageService,
    private val userImageStorageService: UserImageStorageService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ImageFacade {
    @Transactional
    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
    ): UploadedImageDto = imageService.createUploadedImage(file, userId)

    override fun createImage(request: CreateImageRequest): ImageDto {
        // This method is not implemented as it's superseded by createUploadedImage which works with MultipartFile
        // The CreateImageRequest doesn't contain the actual image data needed for implementation
        throw UnsupportedOperationException(
            "This method is not supported. Use createUploadedImage() with MultipartFile for uploading images, " +
                "or use the appropriate generation methods for creating generated images.",
        )
    }

    @Cacheable("uploadedImages", key = "#uuid + '_' + #userId")
    @Transactional(readOnly = true)
    override fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImageDto = imageService.getUploadedImageByUuid(uuid, userId)

    @CacheEvict("uploadedImages", key = "#uuid + '_' + #userId")
    @Transactional
    override fun deleteUploadedImage(
        uuid: UUID,
        userId: Long,
    ) = imageService.deleteUploadedImage(uuid, userId)

    @Cacheable("userUploadedImages", key = "#userId")
    @Transactional(readOnly = true)
    override fun getUserUploadedImages(userId: Long): List<UploadedImageDto> = imageService.getUserUploadedImages(userId)

    // Generated Images Implementation

    @Cacheable("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional(readOnly = true)
    override fun getGeneratedImageByUuid(
        uuid: UUID,
        userId: Long?,
    ): GeneratedImageDto = imageService.getGeneratedImageByUuid(uuid, userId)

    @CacheEvict("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional
    override fun updateGeneratedImage(
        uuid: UUID,
        updateRequest: UpdateGeneratedImageRequest,
        userId: Long?,
    ): GeneratedImageDto = imageService.updateGeneratedImage(uuid, updateRequest, userId)

    @CacheEvict("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional
    override fun deleteGeneratedImage(
        uuid: UUID,
        userId: Long?,
    ) = imageService.deleteGeneratedImage(uuid, userId)

    @Cacheable("userGeneratedImages", key = "#userId")
    @Transactional(readOnly = true)
    override fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto> = imageService.getUserGeneratedImages(userId)
}
