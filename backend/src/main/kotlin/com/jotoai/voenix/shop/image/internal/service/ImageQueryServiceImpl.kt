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
}
