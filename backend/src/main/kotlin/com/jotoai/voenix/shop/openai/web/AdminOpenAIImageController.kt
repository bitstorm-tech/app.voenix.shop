package com.jotoai.voenix.shop.openai.web

import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageEditResponse
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.api.dto.TestPromptResponse
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
    private val openAIImageFacade: OpenAIImageFacade,
) {
    @PostMapping("/image-edit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createImageEdit(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestPart("request") @Valid request: CreateImageEditRequest,
    ): ImageEditResponse = openAIImageFacade.editImage(imageFile, request)

    @PostMapping("/test-prompt", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun testPrompt(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestParam("masterPrompt") masterPrompt: String,
        @RequestParam("specificPrompt", required = false) specificPrompt: String?,
        @RequestParam("background") background: String,
        @RequestParam("quality") quality: String,
        @RequestParam("size") size: String,
    ): TestPromptResponse =
        openAIImageFacade.testPrompt(
            imageFile,
            TestPromptRequest(
                masterPrompt = masterPrompt,
                specificPrompt = specificPrompt ?: "",
                backgroundString = background,
                qualityString = quality,
                sizeString = size,
            ),
        )
}
