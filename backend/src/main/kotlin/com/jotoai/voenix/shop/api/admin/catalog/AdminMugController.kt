package com.jotoai.voenix.shop.api.admin.catalog

import com.jotoai.voenix.shop.domain.mugs.dto.CreateMugRequest
import com.jotoai.voenix.shop.domain.mugs.dto.MugDto
import com.jotoai.voenix.shop.domain.mugs.dto.UpdateMugRequest
import com.jotoai.voenix.shop.domain.mugs.service.MugService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/catalog/mugs")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugController(
    private val mugService: MugService,
) {
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
