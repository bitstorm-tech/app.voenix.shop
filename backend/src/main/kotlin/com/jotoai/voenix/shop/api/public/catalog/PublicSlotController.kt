package com.jotoai.voenix.shop.api.public.catalog

import com.jotoai.voenix.shop.domain.prompts.dto.SlotDto
import com.jotoai.voenix.shop.domain.prompts.service.SlotService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/catalog/slots")
class PublicSlotController(
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
}
