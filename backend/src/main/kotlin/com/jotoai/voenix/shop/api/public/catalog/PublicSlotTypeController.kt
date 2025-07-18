package com.jotoai.voenix.shop.api.public.catalog

import com.jotoai.voenix.shop.domain.prompts.dto.SlotTypeDto
import com.jotoai.voenix.shop.domain.prompts.service.SlotTypeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/catalog/slot-types")
class PublicSlotTypeController(
    private val slotTypeService: SlotTypeService,
) {
    @GetMapping
    fun getAllSlotTypes(): List<SlotTypeDto> = slotTypeService.getAllSlotTypes()

    @GetMapping("/{id}")
    fun getSlotTypeById(
        @PathVariable id: Long,
    ): SlotTypeDto = slotTypeService.getSlotTypeById(id)
}
