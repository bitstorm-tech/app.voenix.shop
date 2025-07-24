package com.jotoai.voenix.shop.api.admin.vat

import com.jotoai.voenix.shop.domain.vat.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.domain.vat.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.domain.vat.dto.ValueAddedTaxDto
import com.jotoai.voenix.shop.domain.vat.service.ValueAddedTaxService
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
    private val valueAddedTaxService: ValueAddedTaxService,
) {
    @GetMapping
    fun getAllVats(): List<ValueAddedTaxDto> = valueAddedTaxService.getAllVats()

    @GetMapping("/{id}")
    fun getVatById(
        @PathVariable id: Long,
    ): ValueAddedTaxDto = valueAddedTaxService.getVatById(id)

    @PostMapping
    fun createVat(
        @Valid @RequestBody createValueAddedTaxRequest: CreateValueAddedTaxRequest,
    ): ValueAddedTaxDto = valueAddedTaxService.createVat(createValueAddedTaxRequest)

    @PutMapping("/{id}")
    fun updateVat(
        @PathVariable id: Long,
        @Valid @RequestBody updateValueAddedTaxRequest: UpdateValueAddedTaxRequest,
    ): ValueAddedTaxDto = valueAddedTaxService.updateVat(id, updateValueAddedTaxRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVat(
        @PathVariable id: Long,
    ) {
        valueAddedTaxService.deleteVat(id)
    }
}
