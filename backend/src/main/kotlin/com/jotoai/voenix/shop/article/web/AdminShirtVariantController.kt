package com.jotoai.voenix.shop.article.web

import com.jotoai.voenix.shop.article.api.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantFacade
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantQueryService
import com.jotoai.voenix.shop.image.api.dto.CropAreaUtils
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.internal.service.ImageStorageServiceImpl
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
@RequestMapping("/api/admin/articles/shirts")
@PreAuthorize("hasRole('ADMIN')")
class AdminShirtVariantController(
    private val shirtVariantQueryService: ShirtVariantQueryService,
    private val shirtVariantFacade: ShirtVariantFacade,
    private val imageStorageService: ImageStorageServiceImpl,
) {
    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<ShirtArticleVariantDto>> {
        val variants = shirtVariantQueryService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateShirtArticleVariantRequest,
    ): ResponseEntity<ShirtArticleVariantDto> {
        val variant = shirtVariantFacade.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateShirtArticleVariantRequest,
    ): ResponseEntity<ShirtArticleVariantDto> {
        val variant = shirtVariantFacade.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        shirtVariantFacade.delete(variantId)
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
    ): ResponseEntity<ShirtArticleVariantDto> {
        // Create crop area if crop parameters are provided
        val cropArea = CropAreaUtils.createIfPresent(cropX, cropY, cropWidth, cropHeight)

        // Store the image directly in the shirt variant images directory
        val storedFilename = imageStorageService.storeFile(file, ImageType.SHIRT_VARIANT_EXAMPLE, cropArea)

        // Update the variant with the new image filename
        val updatedVariant = shirtVariantFacade.updateExampleImage(variantId, storedFilename)

        return ResponseEntity.ok(updatedVariant)
    }
}
