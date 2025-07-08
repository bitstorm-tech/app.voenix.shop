package com.jotoai.voenix.shop.prompts.controller

import com.jotoai.voenix.shop.prompts.dto.CreateSlotRequest
import com.jotoai.voenix.shop.prompts.dto.SlotDto
import com.jotoai.voenix.shop.prompts.dto.UpdateSlotRequest
import com.jotoai.voenix.shop.prompts.service.SlotService
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
@RequestMapping("/api/slots")
class SlotController(
    private val slotService: SlotService,
) {
    @GetMapping
    fun getAllSlots(): ResponseEntity<List<SlotDto>> = ResponseEntity.ok(slotService.getAllSlots())

    @GetMapping("/{id}")
    fun getSlotById(
        @PathVariable id: Long,
    ): ResponseEntity<SlotDto> = ResponseEntity.ok(slotService.getSlotById(id))

    @GetMapping("/by-slot-type/{slotTypeId}")
    fun getSlotsBySlotType(
        @PathVariable slotTypeId: Long,
    ): ResponseEntity<List<SlotDto>> = ResponseEntity.ok(slotService.getSlotsBySlotType(slotTypeId))

    @PostMapping
    fun createSlot(
        @Valid @RequestBody request: CreateSlotRequest,
    ): ResponseEntity<SlotDto> {
        val createdSlot = slotService.createSlot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSlot)
    }

    @PutMapping("/{id}")
    fun updateSlot(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSlotRequest,
    ): ResponseEntity<SlotDto> = ResponseEntity.ok(slotService.updateSlot(id, request))

    @DeleteMapping("/{id}")
    fun deleteSlot(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        slotService.deleteSlot(id)
        return ResponseEntity.noContent().build()
    }
}
