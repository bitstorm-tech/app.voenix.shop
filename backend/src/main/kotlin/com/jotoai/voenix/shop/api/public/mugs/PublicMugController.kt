package com.jotoai.voenix.shop.api.public.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.MugDto
import com.jotoai.voenix.shop.domain.mugs.service.MugService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/mugs")
class PublicMugController(
    private val mugService: MugService,
) {
    @GetMapping
    fun getActiveMugs(): List<MugDto> = mugService.getActiveMugs()

    @GetMapping("/{id}")
    fun getMugById(
        @PathVariable id: Long,
    ): MugDto = mugService.getMugById(id)

    @GetMapping("/search")
    fun searchMugsByName(
        @RequestParam name: String,
    ): List<MugDto> = mugService.searchMugsByName(name)

    @GetMapping("/price-range")
    fun findMugsByPriceRange(
        @RequestParam minPrice: Int,
        @RequestParam maxPrice: Int,
    ): List<MugDto> = mugService.findMugsByPriceRange(minPrice, maxPrice)
}
