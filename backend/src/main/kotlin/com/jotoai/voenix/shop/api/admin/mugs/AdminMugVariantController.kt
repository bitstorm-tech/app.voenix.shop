package com.jotoai.voenix.shop.api.admin.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.CreateMugVariantRequest
import com.jotoai.voenix.shop.domain.mugs.dto.MugVariantDto
import com.jotoai.voenix.shop.domain.mugs.dto.UpdateMugVariantRequest
import com.jotoai.voenix.shop.domain.mugs.service.MugVariantService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/mugs/variants")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugVariantController(
    private val mugVariantService: MugVariantService,
) {
    @GetMapping
    fun getAllMugVariants(): List<MugVariantDto> = mugVariantService.getAllMugVariants()

    @GetMapping("/{id}")
    fun getMugVariantById(
        @PathVariable id: Long,
    ): MugVariantDto = mugVariantService.getMugVariantById(id)

    @GetMapping("/mug/{mugId}")
    fun getMugVariantsByMugId(
        @PathVariable mugId: Long,
    ): List<MugVariantDto> = mugVariantService.getMugVariantsByMugId(mugId)

    @PostMapping
    fun createMugVariant(
        @Valid @RequestBody createMugVariantRequest: CreateMugVariantRequest,
    ): MugVariantDto = mugVariantService.createMugVariant(createMugVariantRequest)

    @PutMapping("/{id}")
    fun updateMugVariant(
        @PathVariable id: Long,
        @Valid @RequestBody updateMugVariantRequest: UpdateMugVariantRequest,
    ): MugVariantDto = mugVariantService.updateMugVariant(id, updateMugVariantRequest)

    @DeleteMapping("/{id}")
    fun deleteMugVariant(
        @PathVariable id: Long,
    ) {
        mugVariantService.deleteMugVariant(id)
    }
}
