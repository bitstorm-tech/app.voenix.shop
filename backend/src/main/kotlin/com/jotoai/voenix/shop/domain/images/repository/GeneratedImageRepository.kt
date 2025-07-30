package com.jotoai.voenix.shop.domain.images.repository

import com.jotoai.voenix.shop.domain.images.entity.GeneratedImage
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GeneratedImageRepository : JpaRepository<GeneratedImage, Long> {
    @Query("SELECT COUNT(g) FROM GeneratedImage g WHERE g.user.id = :userId AND g.generatedAt > :startTime")
    fun countByUserIdAndGeneratedAtAfter(
        @Param("userId") userId: Long,
        @Param("startTime") startTime: LocalDateTime,
    ): Long

    @Query("SELECT COUNT(g) FROM GeneratedImage g WHERE g.ipAddress = :ipAddress AND g.generatedAt > :startTime")
    fun countByIpAddressAndGeneratedAtAfter(
        @Param("ipAddress") ipAddress: String,
        @Param("startTime") startTime: LocalDateTime,
    ): Long
}
