package com.jotoai.voenix.shop.vat.web

import com.jotoai.voenix.shop.vat.api.VatService
import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/vat")
@PreAuthorize("hasRole('ADMIN')")
class AdminValueAddedTaxController(
    private val vatService: VatService,
) {
    @GetMapping
    fun getAllVats(): List<ValueAddedTaxDto> = vatService.getAllVats()

    @GetMapping("/{id}")
    fun getVatById(
        @PathVariable id: Long,
    ): ValueAddedTaxDto = vatService.getVatById(id)

    @PostMapping
    fun createVat(
        @Valid @RequestBody createValueAddedTaxRequest: CreateValueAddedTaxRequest,
    ): ValueAddedTaxDto = vatService.createVat(createValueAddedTaxRequest)

    @PutMapping("/{id}")
    fun updateVat(
        @PathVariable id: Long,
        @Valid @RequestBody updateValueAddedTaxRequest: UpdateValueAddedTaxRequest,
    ): ValueAddedTaxDto = vatService.updateVat(id, updateValueAddedTaxRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVat(
        @PathVariable id: Long,
    ) {
        vatService.deleteVat(id)
    }
}
