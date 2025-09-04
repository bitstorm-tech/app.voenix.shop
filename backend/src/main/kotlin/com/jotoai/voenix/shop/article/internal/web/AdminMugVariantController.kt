package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.internal.dto.CopyVariantsRequest
import com.jotoai.voenix.shop.article.internal.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.internal.dto.MugWithVariantsSummaryDto
import com.jotoai.voenix.shop.article.internal.service.MugVariantServiceImpl
import com.jotoai.voenix.shop.article.internal.web.dto.MugVariantImageUploadRequest
import com.jotoai.voenix.shop.image.CropArea
import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles/mugs")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugVariantController(
    private val mugVariantService: MugVariantServiceImpl,
    private val imageService: ImageService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
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
        @ModelAttribute request: MugVariantImageUploadRequest,
    ): ResponseEntity<MugArticleVariantDto> {
        logger.info {
            (
                """
                Received image upload for variant $variantId
                File: ${request.image.originalFilename}, Size: ${request.image.size} bytes
                Crop params: x=${request.cropX}, y=${request.cropY}, width=${request.cropWidth}, height=${request.cropHeight}
                """.trimIndent()
            )
        }

        // Create crop area if crop parameters are provided
        val cropArea =
            CropArea.fromNullable(
                request.cropX,
                request.cropY,
                request.cropWidth,
                request.cropHeight,
            )

        // Store the image directly in the mug variant images directory
        val imageDto =
            imageService.store(
                data = ImageData.File(request.image, cropArea),
                metadata = ImageMetadata(type = ImageType.MUG_VARIANT_EXAMPLE),
            )
        val storedFilename = imageDto.filename

        // Update the variant with the new image filename
        val updatedVariant = mugVariantService.updateExampleImage(variantId, storedFilename)

        logger.info { "Successfully uploaded image for variant $variantId - Filename: $storedFilename" }
        return ResponseEntity.ok(updatedVariant)
    }

    @DeleteMapping("/variants/{variantId}/image")
    fun deleteVariantImage(
        @PathVariable variantId: Long,
    ): ResponseEntity<MugArticleVariantDto> {
        logger.info { "Deleting image for variant $variantId" }
        val updatedVariant = mugVariantService.removeExampleImage(variantId)
        return ResponseEntity.ok(updatedVariant)
    }

    @GetMapping("/variants-catalog")
    fun getVariantsCatalog(
        @RequestParam(required = false) excludeMugId: Long?,
    ): ResponseEntity<List<MugWithVariantsSummaryDto>> {
        logger.info { "Fetching variants catalog, excluding mug ID: $excludeMugId" }
        val catalog = mugVariantService.findAllMugsWithVariants(excludeMugId)
        return ResponseEntity.ok(catalog)
    }

    @PostMapping("/{mugId}/copy-variants")
    fun copyVariants(
        @PathVariable mugId: Long,
        @Valid @RequestBody request: CopyVariantsRequest,
    ): ResponseEntity<List<MugArticleVariantDto>> {
        logger.info { "Copying ${request.variantIds.size} variants to mug $mugId" }
        val copiedVariants = mugVariantService.copyVariants(mugId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(copiedVariants)
    }
}
