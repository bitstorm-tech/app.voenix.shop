package com.jotoai.voenix.shop.image.internal.repository

import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

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
     * Check if a generated image exists by ID and user ID
     */
    fun existsByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean

    /**
     * Find paginated generated images by user ID
     */
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<GeneratedImage>
}
