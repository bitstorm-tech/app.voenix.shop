package com.jotoai.voenix.shop.mugs.controller

import com.jotoai.voenix.shop.mugs.dto.CreateMugRequest
import com.jotoai.voenix.shop.mugs.dto.MugDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugRequest
import com.jotoai.voenix.shop.mugs.service.MugService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mugs")
class MugController(
    private val mugService: MugService,
) {
    @GetMapping
    fun getAllMugs(): ResponseEntity<List<MugDto>> = ResponseEntity.ok(mugService.getAllMugs())

    @GetMapping("/active")
    fun getActiveMugs(): ResponseEntity<List<MugDto>> = ResponseEntity.ok(mugService.getActiveMugs())

    @GetMapping("/{id}")
    fun getMugById(
        @PathVariable id: Long,
    ): ResponseEntity<MugDto> = ResponseEntity.ok(mugService.getMugById(id))

    @GetMapping("/search")
    fun searchMugs(
        @RequestParam name: String,
    ): ResponseEntity<List<MugDto>> = ResponseEntity.ok(mugService.searchMugsByName(name))

    @GetMapping("/price-range")
    fun getMugsByPriceRange(
        @RequestParam minPrice: Int,
        @RequestParam maxPrice: Int,
    ): ResponseEntity<List<MugDto>> = ResponseEntity.ok(mugService.findMugsByPriceRange(minPrice, maxPrice))

    @PostMapping
    fun createMug(
        @Valid @RequestBody request: CreateMugRequest,
    ): ResponseEntity<MugDto> {
        val createdMug = mugService.createMug(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMug)
    }

    @PutMapping("/{id}")
    fun updateMug(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMugRequest,
    ): ResponseEntity<MugDto> = ResponseEntity.ok(mugService.updateMug(id, request))

    @DeleteMapping("/{id}")
    fun deleteMug(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        mugService.deleteMug(id)
        return ResponseEntity.noContent().build()
    }
}
