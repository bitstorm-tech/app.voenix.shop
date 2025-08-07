package com.jotoai.voenix.shop.image.internal.repository

import com.jotoai.voenix.shop.image.internal.domain.GeneratedImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface GeneratedImageRepository : JpaRepository<GeneratedImage, Long> {
    @Query("SELECT COUNT(g) FROM GeneratedImage g WHERE g.userId = :userId AND g.generatedAt > :startTime")
    fun countByUserIdAndGeneratedAtAfter(
        @Param("userId") userId: Long,
        @Param("startTime") startTime: LocalDateTime,
    ): Long

    @Query("SELECT COUNT(g) FROM GeneratedImage g WHERE g.ipAddress = :ipAddress AND g.generatedAt > :startTime")
    fun countByIpAddressAndGeneratedAtAfter(
        @Param("ipAddress") ipAddress: String,
        @Param("startTime") startTime: LocalDateTime,
    ): Long

    fun findByFilename(filename: String): GeneratedImage?

    /**
     * Find a generated image by UUID
     */
    fun findByUuid(uuid: UUID): GeneratedImage?

    /**
     * Find generated images by user ID
     */
    fun findAllByUserId(userId: Long): List<GeneratedImage>

    /**
     * Find generated images by user ID with fetch join to avoid N+1 queries
     */
    @Query("SELECT g FROM GeneratedImage g LEFT JOIN FETCH g.uploadedImage WHERE g.userId = :userId")
    fun findAllByUserIdWithUploadedImage(
        @Param("userId") userId: Long,
    ): List<GeneratedImage>

    /**
     * Find a generated image by UUID and user ID
     */
    fun findByUuidAndUserId(
        uuid: UUID,
        userId: Long,
    ): GeneratedImage?

    /**
     * Check if a generated image exists by ID and user ID
     */
    fun existsByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean
}
