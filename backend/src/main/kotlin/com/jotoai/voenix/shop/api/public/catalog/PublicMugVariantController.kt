package com.jotoai.voenix.shop.api.public.catalog

import com.jotoai.voenix.shop.domain.mugs.dto.MugVariantDto
import com.jotoai.voenix.shop.domain.mugs.service.MugVariantService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/catalog/mug-variants")
class PublicMugVariantController(
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
}
