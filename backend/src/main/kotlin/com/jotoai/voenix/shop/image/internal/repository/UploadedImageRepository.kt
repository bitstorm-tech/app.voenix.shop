package com.jotoai.voenix.shop.image.internal.repository

import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    /**
     * Find all uploaded images by user ID with generated images to avoid N+1 queries
     */
    @Query(
        "SELECT u FROM UploadedImage u LEFT JOIN FETCH u.generatedImages " +
            "WHERE u.userId = :userId ORDER BY u.uploadedAt DESC",
    )
    fun findAllByUserIdWithGeneratedImages(
        @Param("userId") userId: Long,
    ): List<UploadedImage>
}
