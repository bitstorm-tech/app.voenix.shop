package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ImageQueryServiceImpl(
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
) : ImageQueryService {

    override fun findUploadedImageByUuid(uuid: UUID): ImageDto? {
        val uploaded = uploadedImageRepository.findByUuid(uuid)
        return uploaded?.let {
            SimpleImageDto(filename = it.storedFilename, imageType = ImageType.PRIVATE)
        }
    }

    override fun findUploadedImagesByUserId(userId: Long): List<ImageDto> =
        uploadedImageRepository.findAllByUserId(userId).map {
            SimpleImageDto(filename = it.storedFilename, imageType = ImageType.PRIVATE)
        }

    override fun existsByUuid(uuid: UUID): Boolean = 
        uploadedImageRepository.findByUuid(uuid) != null

    override fun existsByUuidAndUserId(uuid: UUID, userId: Long): Boolean = 
        uploadedImageRepository.findByUserIdAndUuid(userId, uuid) != null

    override fun existsGeneratedImageById(id: Long): Boolean = 
        generatedImageRepository.existsById(id)

    override fun existsGeneratedImageByIdAndUserId(id: Long, userId: Long): Boolean = 
        generatedImageRepository.existsByIdAndUserId(id, userId)

    override fun validateGeneratedImageOwnership(imageId: Long, userId: Long?): Boolean =
        if (userId != null) {
            existsGeneratedImageByIdAndUserId(imageId, userId)
        } else {
            existsGeneratedImageById(imageId)
        }

    override fun findGeneratedImageById(id: Long): GeneratedImageDto? {
        val generated = generatedImageRepository.findById(id).orElse(null)
        return generated?.let {
            GeneratedImageDto(
                filename = it.filename,
                imageType = ImageType.GENERATED,
                promptId = it.promptId,
                userId = it.userId,
                generatedAt = it.generatedAt,
                ipAddress = it.ipAddress,
            )
        }
    }

    override fun findGeneratedImagesByIds(ids: List<Long>): Map<Long, GeneratedImageDto> {
        if (ids.isEmpty()) return emptyMap()
        return generatedImageRepository.findAllById(ids).associateBy(
            { requireNotNull(it.id) { "GeneratedImage ID cannot be null" } },
            {
                GeneratedImageDto(
                    filename = it.filename,
                    imageType = ImageType.GENERATED,
                    promptId = it.promptId,
                    userId = it.userId,
                    generatedAt = it.generatedAt,
                    ipAddress = it.ipAddress,
                )
            },
        )
    }
}
