package com.jotoai.voenix.shop.image.internal.repository

import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UploadedImageRepository : JpaRepository<UploadedImage, Long> {
    fun findByUserIdAndUuid(
        userId: Long,
        uuid: UUID,
    ): UploadedImage?

    /**
     * Find paginated uploaded images by user ID
     */
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<UploadedImage>
}
