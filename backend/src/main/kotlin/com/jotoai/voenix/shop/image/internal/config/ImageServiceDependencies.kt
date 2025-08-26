package com.jotoai.voenix.shop.image.internal.config

import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.internal.service.ImageValidationService
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
import org.springframework.stereotype.Component

@Component
data class ImageServiceDependencies(
    // Core image services
    val imageStorageService: ImageStorageService,
    val imageValidationService: ImageValidationService,
    val storagePathService: StoragePathService,
    
    // Repository dependencies
    val uploadedImageRepository: UploadedImageRepository,
    val generatedImageRepository: GeneratedImageRepository,
    
    // External service dependencies
    val openAIImageGenerationService: OpenAIImageGenerationService,
    val promptQueryService: PromptQueryService,
    val userService: UserService,
)