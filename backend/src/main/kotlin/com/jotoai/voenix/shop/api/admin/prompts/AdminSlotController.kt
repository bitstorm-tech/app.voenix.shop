package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.CreateSlotRequest
import com.jotoai.voenix.shop.domain.prompts.dto.SlotDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdateSlotRequest
import com.jotoai.voenix.shop.domain.prompts.service.SlotService
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
@RequestMapping("/api/admin/prompts/slots")
@PreAuthorize("hasRole('ADMIN')")
class AdminSlotController(
    private val slotService: SlotService,
) {
    @GetMapping
    fun getAllSlots(): List<SlotDto> = slotService.getAllSlots()

    @GetMapping("/{id}")
    fun getSlotById(
        @PathVariable id: Long,
    ): SlotDto = slotService.getSlotById(id)

    @GetMapping("/type/{typeId}")
    fun getSlotsByTypeId(
        @PathVariable typeId: Long,
    ): List<SlotDto> = slotService.getSlotsBySlotType(typeId)

    @PostMapping
    fun createSlot(
        @Valid @RequestBody createSlotRequest: CreateSlotRequest,
    ): SlotDto = slotService.createSlot(createSlotRequest)

    @PutMapping("/{id}")
    fun updateSlot(
        @PathVariable id: Long,
        @Valid @RequestBody updateSlotRequest: UpdateSlotRequest,
    ): SlotDto = slotService.updateSlot(id, updateSlotRequest)

    @DeleteMapping("/{id}")
    fun deleteSlot(
        @PathVariable id: Long,
    ) {
        slotService.deleteSlot(id)
    }
}
