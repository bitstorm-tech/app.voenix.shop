package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.prompt.api.PromptFacade
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.UpdatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.AddSlotVariantsRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantsRequest
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
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptController(
    private val promptQueryService: PromptQueryService,
    private val promptFacade: PromptFacade? = null,
) {
    private val writeFacade: PromptFacade
        get() = requireNotNull(promptFacade) { "PromptFacade bean is required for write operations" }

    @GetMapping
    fun getAllPrompts(): List<PromptDto> = promptQueryService.getAllPrompts()

    @GetMapping("/{id}")
    fun getPromptById(
        @PathVariable id: Long,
    ): PromptDto = promptQueryService.getPromptById(id)

    @GetMapping("/search")
    fun searchPromptsByTitle(
        @RequestParam title: String,
    ): List<PromptDto> = promptQueryService.searchPromptsByTitle(title)

    @PostMapping
    fun createPrompt(
        @Valid @RequestBody createPromptRequest: CreatePromptRequest,
    ): PromptDto = writeFacade.createPrompt(createPromptRequest)

    @PutMapping("/{id}")
    fun updatePrompt(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptRequest: UpdatePromptRequest,
    ): PromptDto = writeFacade.updatePrompt(id, updatePromptRequest)

    @PutMapping("/{id}/slots")
    fun updatePromptSlotVariants(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotVariantsRequest: UpdatePromptSlotVariantsRequest,
    ): PromptDto = writeFacade.updatePromptSlotVariants(id, updatePromptSlotVariantsRequest)

    @PostMapping("/{id}/slots")
    fun addSlotVariantsToPrompt(
        @PathVariable id: Long,
        @Valid @RequestBody addSlotVariantsRequest: AddSlotVariantsRequest,
    ): PromptDto = writeFacade.addSlotVariantsToPrompt(id, addSlotVariantsRequest)

    @DeleteMapping("/{id}")
    fun deletePrompt(
        @PathVariable id: Long,
    ) {
        writeFacade.deletePrompt(id)
    }
}
