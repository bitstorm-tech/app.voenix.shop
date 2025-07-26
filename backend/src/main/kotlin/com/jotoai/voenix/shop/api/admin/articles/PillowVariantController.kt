package com.jotoai.voenix.shop.api.admin.articles

import com.jotoai.voenix.shop.domain.articles.dto.ArticlePillowVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticlePillowVariantRequest
import com.jotoai.voenix.shop.domain.articles.service.PillowVariantService
import com.jotoai.voenix.shop.domain.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.domain.images.dto.CropArea
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.ImageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/articles/pillows")
@PreAuthorize("hasRole('ADMIN')")
class PillowVariantController(
    private val pillowVariantService: PillowVariantService,
    private val imageService: ImageService,
) {
    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<ArticlePillowVariantDto>> {
        val variants = pillowVariantService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateArticlePillowVariantRequest,
    ): ResponseEntity<ArticlePillowVariantDto> {
        val variant = pillowVariantService.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateArticlePillowVariantRequest,
    ): ResponseEntity<ArticlePillowVariantDto> {
        val variant = pillowVariantService.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        pillowVariantService.delete(variantId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/variants/{variantId}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadVariantImage(
        @PathVariable variantId: Long,
        @RequestParam("image") file: MultipartFile,
        @RequestParam("cropX", required = false) cropX: Double?,
        @RequestParam("cropY", required = false) cropY: Double?,
        @RequestParam("cropWidth", required = false) cropWidth: Double?,
        @RequestParam("cropHeight", required = false) cropHeight: Double?,
    ): ResponseEntity<ArticlePillowVariantDto> {
        val cropArea =
            if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
                CropArea(x = cropX, y = cropY, width = cropWidth, height = cropHeight)
            } else {
                null
            }

        val imageRequest =
            CreateImageRequest(
                imageType = ImageType.PILLOW_VARIANT_EXAMPLE,
                cropArea = cropArea,
            )

        val imageDto = imageService.store(file, imageRequest)
        val updatedVariant = pillowVariantService.updateExampleImage(variantId, imageDto.filename)

        return ResponseEntity.ok(updatedVariant)
    }
}
