package com.jotoai.voenix.shop.api.admin.services

import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/openai")
@PreAuthorize("hasRole('ADMIN')")
class AdminOpenAIImageController(
    private val openAIImageService: OpenAIImageService,
) {
    @PostMapping("/image-edit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createImageEdit(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestPart("request") @Valid request: CreateImageEditRequest,
    ): ImageEditResponse = openAIImageService.editImage(imageFile, request)

    @PostMapping("/test-prompt", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun testPrompt(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestParam("masterPrompt") masterPrompt: String,
        @RequestParam("specificPrompt", required = false) specificPrompt: String?,
        @RequestParam("background") background: String,
        @RequestParam("quality") quality: String,
        @RequestParam("size") size: String,
    ): TestPromptResponse =
        openAIImageService.testPrompt(
            imageFile,
            TestPromptRequest(
                masterPrompt = masterPrompt,
                specificPrompt = specificPrompt ?: "",
                background =
                    com.jotoai.voenix.shop.domain.openai.dto.enums.ImageBackground
                        .valueOf(background.uppercase()),
                quality =
                    com.jotoai.voenix.shop.domain.openai.dto.enums.ImageQuality
                        .valueOf(quality.uppercase()),
                size =
                    when (size) {
                        "1024x1024" -> com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize.SQUARE_1024X1024
                        "1536x1024" -> com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize.LANDSCAPE_1536X1024
                        "1024x1536" -> com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize.PORTRAIT_1024X1536
                        else -> com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize.SQUARE_1024X1024
                    },
            ),
        )
}
