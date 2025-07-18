package com.jotoai.voenix.shop.api.public.services

import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditResponse
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/public/openai")
class PublicOpenAIImageController(
    private val openAIImageService: OpenAIImageService,
) {
    @PostMapping("/image-edit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createImageEdit(
        @RequestParam("image") imageFile: MultipartFile,
        @RequestPart("request") @Valid request: CreateImageEditRequest,
    ): ImageEditResponse = openAIImageService.editImage(imageFile, request)
}
