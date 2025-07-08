package com.jotoai.voenix.shop.prompts.controller

import com.jotoai.voenix.shop.prompts.dto.CreateSlotTypeRequest
import com.jotoai.voenix.shop.prompts.dto.SlotTypeDto
import com.jotoai.voenix.shop.prompts.dto.UpdateSlotTypeRequest
import com.jotoai.voenix.shop.prompts.service.SlotTypeService
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
@RequestMapping("/api/slot-types")
class SlotTypeController(
    private val slotTypeService: SlotTypeService,
) {
    @GetMapping
    fun getAllSlotTypes(): ResponseEntity<List<SlotTypeDto>> = ResponseEntity.ok(slotTypeService.getAllSlotTypes())

    @GetMapping("/{id}")
    fun getSlotTypeById(
        @PathVariable id: Long,
    ): ResponseEntity<SlotTypeDto> = ResponseEntity.ok(slotTypeService.getSlotTypeById(id))

    @PostMapping
    fun createSlotType(
        @Valid @RequestBody request: CreateSlotTypeRequest,
    ): ResponseEntity<SlotTypeDto> {
        val createdSlotType = slotTypeService.createSlotType(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSlotType)
    }

    @PutMapping("/{id}")
    fun updateSlotType(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSlotTypeRequest,
    ): ResponseEntity<SlotTypeDto> = ResponseEntity.ok(slotTypeService.updateSlotType(id, request))

    @DeleteMapping("/{id}")
    fun deleteSlotType(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        slotTypeService.deleteSlotType(id)
        return ResponseEntity.noContent().build()
    }
}
