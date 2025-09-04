package com.jotoai.voenix.shop.openai.internal.web

import com.jotoai.voenix.shop.openai.internal.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.internal.dto.ImageEditResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.service.OpenAIImageService
import com.jotoai.voenix.shop.openai.internal.web.dto.TestPromptForm
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminOpenAIImageController(
    private val openAIImageService: OpenAIImageService,
) {
    @PostMapping("/image-edit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createImageEdit(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestPart("request") @Valid request: CreateImageEditRequest,
        @RequestParam("provider", required = false, defaultValue = "OPENAI") provider: String,
    ): ImageEditResponse =
        openAIImageService.editImage(
            imageFile,
            request,
            OpenAIImageService.AiProvider.valueOf(provider.uppercase()),
        )

    @PostMapping("/test-prompt", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun testPrompt(
        @ModelAttribute form: TestPromptForm,
        @RequestParam("provider", required = false, defaultValue = "OPENAI") provider: String,
    ): TestPromptResponse =
        openAIImageService.testPrompt(
            form.image,
            TestPromptRequest(
                masterPrompt = form.masterPrompt,
                specificPrompt = form.specificPrompt ?: "",
                backgroundString = form.background,
                qualityString = form.quality,
                sizeString = form.size,
            ),
            OpenAIImageService.AiProvider.valueOf(provider.uppercase()),
        )
}
