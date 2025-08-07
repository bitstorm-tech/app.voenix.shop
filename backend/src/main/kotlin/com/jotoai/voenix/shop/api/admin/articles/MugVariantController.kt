package com.jotoai.voenix.shop.api.admin.articles

import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.service.MugVariantService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
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
@RequestMapping("/api/admin/articles/mugs")
@PreAuthorize("hasRole('ADMIN')")
class MugVariantController(
    private val mugVariantService: MugVariantService,
    private val imageFacade: ImageFacade,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MugVariantController::class.java)
    }

    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<MugArticleVariantDto>> {
        val variants = mugVariantService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateMugArticleVariantRequest,
    ): ResponseEntity<MugArticleVariantDto> {
        val variant = mugVariantService.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateMugArticleVariantRequest,
    ): ResponseEntity<MugArticleVariantDto> {
        val variant = mugVariantService.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        mugVariantService.delete(variantId)
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
    ): ResponseEntity<MugArticleVariantDto> {
        logger.info(
            "Received image upload for variant $variantId - File: ${file.originalFilename}, " +
                "Size: ${file.size} bytes, Crop params: x=$cropX, y=$cropY, width=$cropWidth, height=$cropHeight",
        )

        val cropArea =
            if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
                CropArea(x = cropX, y = cropY, width = cropWidth, height = cropHeight)
            } else {
                null
            }

        val imageRequest =
            CreateImageRequest(
                imageType = ImageType.MUG_VARIANT_EXAMPLE,
                cropArea = cropArea,
            )

        val imageDto = imageFacade.createImage(imageRequest)
        val updatedVariant = mugVariantService.updateExampleImage(variantId, imageDto.filename)

        return ResponseEntity.ok(updatedVariant)
    }

    @DeleteMapping("/variants/{variantId}/image")
    fun deleteVariantImage(
        @PathVariable variantId: Long,
    ): ResponseEntity<MugArticleVariantDto> {
        logger.info("Deleting image for variant $variantId")
        val updatedVariant = mugVariantService.removeExampleImage(variantId)
        return ResponseEntity.ok(updatedVariant)
    }
}
