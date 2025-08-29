package com.jotoai.voenix.shop.openai.web

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CropAreaUtils
import com.jotoai.voenix.shop.openai.api.ImageGenerationService
import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationResponse
import com.jotoai.voenix.shop.openai.web.dto.ImageGenerationForm
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/openai/images")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
class UserImageGenerationController(
    private val imageFacade: ImageFacade,
    private val imageGenerationService: ImageGenerationService,
    private val userService: UserService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateImage(
        @ModelAttribute form: ImageGenerationForm,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ImageGenerationResponse {
        val user = userService.getUserByEmail(userDetails.username)
        logger.info { "Image generation request: user=${user.id}, promptId=${form.promptId}" }

        // Create crop area if all crop parameters are provided
        val cropArea = CropAreaUtils.createIfPresent(form.cropX, form.cropY, form.cropWidth, form.cropHeight)

        // First upload the image to get UUID, applying crop if provided
        val uploadedImage = imageFacade.createUploadedImage(form.image, user.id, cropArea)
        logger.info { "Uploaded image for user ${user.id} with UUID: ${uploadedImage.uuid}" }

        // Generate all 4 images; crop already applied at upload time, so avoid double-cropping here
        val response = imageGenerationService.generateUserImageWithIds(form.promptId, uploadedImage.uuid, user.id, null)

        logger.info { "Generated ${response.generatedImageIds.size} images for user ${user.id}" }

        return response
    }
}
