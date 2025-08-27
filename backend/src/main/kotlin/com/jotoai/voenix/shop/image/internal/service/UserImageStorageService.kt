package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import org.springframework.web.multipart.MultipartFile

/**
 * Internal service interface for user-specific image storage operations.
 * This avoids downcasting to implementation classes from higher-level services.
 */
interface UserImageStorageService {
    fun storeUploadedImage(imageFile: MultipartFile, userId: Long, cropArea: CropArea? = null): UploadedImage

    fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImage: UploadedImage,
        promptId: Long,
        generationNumber: Int,
        ipAddress: String? = null,
    ): GeneratedImage

    fun getUserImageData(filename: String, userId: Long): Pair<ByteArray, String>

    fun deleteUserImage(filename: String, userId: Long): Boolean
}
