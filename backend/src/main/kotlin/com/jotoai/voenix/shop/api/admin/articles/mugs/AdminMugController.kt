package com.jotoai.voenix.shop.api.admin.articles.mugs

import com.jotoai.voenix.shop.domain.articles.mugs.dto.CreateMugRequest
import com.jotoai.voenix.shop.domain.articles.mugs.dto.MugDto
import com.jotoai.voenix.shop.domain.articles.mugs.dto.UpdateMugRequest
import com.jotoai.voenix.shop.domain.articles.mugs.service.MugService
import jakarta.validation.Valid
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

@RestController
@RequestMapping("/api/admin/articles/mugs")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugController(
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

    @PostMapping
    fun createMug(
        @Valid @RequestBody createMugRequest: CreateMugRequest,
    ): MugDto = mugService.createMug(createMugRequest)

    @PutMapping("/{id}")
    fun updateMug(
        @PathVariable id: Long,
        @Valid @RequestBody updateMugRequest: UpdateMugRequest,
    ): MugDto = mugService.updateMug(id, updateMugRequest)

    @DeleteMapping("/{id}")
    fun deleteMug(
        @PathVariable id: Long,
    ) {
        mugService.deleteMug(id)
    }
}
