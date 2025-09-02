package com.jotoai.voenix.shop.image.internal.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "uploaded_images")
@Suppress("LongParameterList")
class UploadedImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    var uuid: UUID,
    @Column(name = "original_filename", nullable = false)
    var originalFilename: String,
    @Column(name = "stored_filename", nullable = false, unique = true)
    var storedFilename: String,
    @Column(name = "content_type", nullable = false, length = 100)
    var contentType: String,
    @Column(name = "file_size", nullable = false)
    var fileSize: Long,
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    val createdAt: OffsetDateTime? = null,
    @OneToMany(mappedBy = "uploadedImage", fetch = FetchType.LAZY)
    var generatedImages: MutableList<GeneratedImage> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadedImage) return false
        return uuid == other.uuid
    }

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String =
        "UploadedImage(id=$id, uuid=$uuid, originalFilename='$originalFilename', storedFilename='$storedFilename')"
}
