package com.jotoai.voenix.shop.article.web

import com.jotoai.voenix.shop.article.api.dto.CopyVariantsRequest
import com.jotoai.voenix.shop.article.api.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.MugWithVariantsSummaryDto
import com.jotoai.voenix.shop.article.api.variants.MugVariantFacade
import com.jotoai.voenix.shop.article.api.variants.MugVariantQueryService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.internal.service.ImageStorageServiceImpl
import io.github.oshai.kotlinlogging.KotlinLogging
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
@RequestMapping("/api/admin/articles/mugs")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugVariantController(
    private val mugVariantQueryService: MugVariantQueryService,
    private val mugVariantFacade: MugVariantFacade,
    private val imageStorageService: ImageStorageServiceImpl,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<MugArticleVariantDto>> {
        val variants = mugVariantQueryService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateMugArticleVariantRequest,
    ): ResponseEntity<MugArticleVariantDto> {
        val variant = mugVariantFacade.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateMugArticleVariantRequest,
    ): ResponseEntity<MugArticleVariantDto> {
        val variant = mugVariantFacade.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        mugVariantFacade.delete(variantId)
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
        logger.info {
            "Received image upload for variant $variantId - File: ${file.originalFilename}, " +
                "Size: ${file.size} bytes, Crop params: x=$cropX, y=$cropY, width=$cropWidth, height=$cropHeight"
        }

        // Create crop area if crop parameters are provided
        val cropArea =
            if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
                CropArea(x = cropX, y = cropY, width = cropWidth, height = cropHeight)
            } else {
                null
            }

        // Store the image directly in the mug variant images directory
        val storedFilename = imageStorageService.storeMugVariantImage(file, cropArea)

        // Update the variant with the new image filename
        val updatedVariant = mugVariantFacade.updateExampleImage(variantId, storedFilename)

        logger.info { "Successfully uploaded image for variant $variantId - Filename: $storedFilename" }
        return ResponseEntity.ok(updatedVariant)
    }

    @DeleteMapping("/variants/{variantId}/image")
    fun deleteVariantImage(
        @PathVariable variantId: Long,
    ): ResponseEntity<MugArticleVariantDto> {
        logger.info { "Deleting image for variant $variantId" }
        val updatedVariant = mugVariantFacade.removeExampleImage(variantId)
        return ResponseEntity.ok(updatedVariant)
    }

    @GetMapping("/variants-catalog")
    fun getVariantsCatalog(
        @RequestParam(required = false) excludeMugId: Long?,
    ): ResponseEntity<List<MugWithVariantsSummaryDto>> {
        logger.info { "Fetching variants catalog, excluding mug ID: $excludeMugId" }
        val catalog = mugVariantQueryService.findAllMugsWithVariants(excludeMugId)
        return ResponseEntity.ok(catalog)
    }

    @PostMapping("/{mugId}/copy-variants")
    fun copyVariants(
        @PathVariable mugId: Long,
        @Valid @RequestBody request: CopyVariantsRequest,
    ): ResponseEntity<List<MugArticleVariantDto>> {
        logger.info { "Copying ${request.variantIds.size} variants to mug $mugId" }
        val copiedVariants = mugVariantFacade.copyVariants(mugId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(copiedVariants)
    }
}
