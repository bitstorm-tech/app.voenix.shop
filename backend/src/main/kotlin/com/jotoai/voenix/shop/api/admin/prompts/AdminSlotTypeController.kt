package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.CreateSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.dto.SlotTypeDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdateSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.service.SlotTypeService
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
@RequestMapping("/api/admin/prompts/slot-types")
@PreAuthorize("hasRole('ADMIN')")
class AdminSlotTypeController(
    private val slotTypeService: SlotTypeService,
) {
    @GetMapping
    fun getAllSlotTypes(): List<SlotTypeDto> = slotTypeService.getAllSlotTypes()

    @GetMapping("/{id}")
    fun getSlotTypeById(
        @PathVariable id: Long,
    ): SlotTypeDto = slotTypeService.getSlotTypeById(id)

    @PostMapping
    fun createSlotType(
        @Valid @RequestBody createSlotTypeRequest: CreateSlotTypeRequest,
    ): SlotTypeDto = slotTypeService.createSlotType(createSlotTypeRequest)

    @PutMapping("/{id}")
    fun updateSlotType(
        @PathVariable id: Long,
        @Valid @RequestBody updateSlotTypeRequest: UpdateSlotTypeRequest,
    ): SlotTypeDto = slotTypeService.updateSlotType(id, updateSlotTypeRequest)

    @DeleteMapping("/{id}")
    fun deleteSlotType(
        @PathVariable id: Long,
    ) {
        slotTypeService.deleteSlotType(id)
    }
}
