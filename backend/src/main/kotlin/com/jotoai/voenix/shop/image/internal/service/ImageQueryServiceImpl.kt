package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Implementation of ImageQueryService that provides read-only operations.
 */
@Service
class ImageQueryServiceImpl(
    private val generatedImageRepository: GeneratedImageRepository,
    private val uploadedImageRepository: UploadedImageRepository,
) : ImageQueryService {
    override fun findImageByFilename(filename: String): ImageDto? {
        val generatedImage = generatedImageRepository.findByFilename(filename)
        return generatedImage?.let {
            SimpleImageDto(
                filename = it.filename,
                imageType = ImageType.GENERATED,
            )
        }
    }

    override fun findUploadedImageByUuid(uuid: UUID): ImageDto? {
        val uploadedImage = uploadedImageRepository.findByUuid(uuid)
        return uploadedImage?.let {
            SimpleImageDto(
                filename = it.storedFilename,
                imageType = ImageType.PRIVATE,
            )
        }
    }

    override fun findUploadedImagesByUserId(userId: Long): List<ImageDto> =
        uploadedImageRepository.findAllByUserId(userId).map {
            SimpleImageDto(
                filename = it.storedFilename,
                imageType = ImageType.PRIVATE,
            )
        }

    override fun existsByUuid(uuid: UUID): Boolean = uploadedImageRepository.findByUuid(uuid) != null

    override fun existsByUuidAndUserId(
        uuid: UUID,
        userId: Long,
    ): Boolean = uploadedImageRepository.findByUserIdAndUuid(userId, uuid) != null

    override fun existsGeneratedImageById(id: Long): Boolean = generatedImageRepository.existsById(id)

    override fun existsGeneratedImageByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean = generatedImageRepository.existsByIdAndUserId(id, userId)

    override fun validateGeneratedImageOwnership(
        imageId: Long,
        userId: Long?,
    ): Boolean =
        if (userId != null) {
            // For authenticated users, check ownership
            existsGeneratedImageByIdAndUserId(imageId, userId)
        } else {
            // For anonymous users, only check existence
            existsGeneratedImageById(imageId)
        }
}
