package com.jotoai.voenix.shop.mugs.controller

import com.jotoai.voenix.shop.mugs.dto.CreateMugVariantRequest
import com.jotoai.voenix.shop.mugs.dto.MugVariantDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugVariantRequest
import com.jotoai.voenix.shop.mugs.service.MugVariantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MugVariantController(
    private val mugVariantService: MugVariantService,
) {
    @GetMapping("/mug-variants")
    fun getAllMugVariants(): ResponseEntity<List<MugVariantDto>> = ResponseEntity.ok(mugVariantService.getAllMugVariants())

    @GetMapping("/mug-variants/{id}")
    fun getMugVariantById(
        @PathVariable id: Long,
    ): ResponseEntity<MugVariantDto> = ResponseEntity.ok(mugVariantService.getMugVariantById(id))

    @GetMapping("/mugs/{mugId}/variants")
    fun getMugVariantsByMugId(
        @PathVariable mugId: Long,
    ): ResponseEntity<List<MugVariantDto>> = ResponseEntity.ok(mugVariantService.getMugVariantsByMugId(mugId))

    @PostMapping("/mug-variants")
    fun createMugVariant(
        @Valid @RequestBody request: CreateMugVariantRequest,
    ): ResponseEntity<MugVariantDto> {
        val createdMugVariant = mugVariantService.createMugVariant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMugVariant)
    }

    @PutMapping("/mug-variants/{id}")
    fun updateMugVariant(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMugVariantRequest,
    ): ResponseEntity<MugVariantDto> = ResponseEntity.ok(mugVariantService.updateMugVariant(id, request))

    @DeleteMapping("/mug-variants/{id}")
    fun deleteMugVariant(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        mugVariantService.deleteMugVariant(id)
        return ResponseEntity.noContent().build()
    }
}
