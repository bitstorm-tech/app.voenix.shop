package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ImageQueryServiceImpl(
    private val generatedImageRepository: GeneratedImageRepository,
) : ImageQueryService {

    override fun existsGeneratedImageById(id: Long): Boolean = 
        generatedImageRepository.existsById(id)

    override fun validateGeneratedImageOwnership(imageId: Long, userId: Long?): Boolean =
        if (userId != null) {
            generatedImageRepository.existsByIdAndUserId(imageId, userId)
        } else {
            generatedImageRepository.existsById(imageId)
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
