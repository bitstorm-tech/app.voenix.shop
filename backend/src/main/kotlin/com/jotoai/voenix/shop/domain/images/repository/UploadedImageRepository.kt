package com.jotoai.voenix.shop.domain.images.repository

import com.jotoai.voenix.shop.domain.images.entity.UploadedImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UploadedImageRepository : JpaRepository<UploadedImage, Long> {
    fun findByUuid(uuid: UUID): UploadedImage?

    fun findByStoredFilename(storedFilename: String): UploadedImage?

    fun findByUserIdAndUuid(
        userId: Long,
        uuid: UUID,
    ): UploadedImage?

    fun findAllByUserId(userId: Long): List<UploadedImage>
}
