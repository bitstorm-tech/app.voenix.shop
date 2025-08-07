package com.jotoai.voenix.shop.image.internal.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "generated_images")
class GeneratedImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var filename: String,
    @Column(name = "prompt_id", nullable = false)
    var promptId: Long,
    @Column(name = "user_id", nullable = true)
    var userId: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_image_id", nullable = true)
    var uploadedImage: UploadedImage? = null,
    @Column(name = "generated_at", nullable = false)
    var generatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "ip_address")
    var ipAddress: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratedImage) return false
        return filename == other.filename
    }

    override fun hashCode(): Int = filename.hashCode()
}
