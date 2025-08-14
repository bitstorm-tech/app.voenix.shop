package com.jotoai.voenix.shop.api.admin.suppliers

import com.jotoai.voenix.shop.supplier.api.SupplierService
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
@RequestMapping("/api/admin/suppliers")
@PreAuthorize("hasRole('ADMIN')")
class AdminSupplierController(
    private val supplierService: SupplierService,
) {
    @GetMapping
    fun getAllSuppliers(): ResponseEntity<List<SupplierDto>> = ResponseEntity.ok(supplierService.getAllSuppliers())

    @GetMapping("/{id}")
    fun getSupplierById(
        @PathVariable id: Long,
    ): ResponseEntity<SupplierDto> = ResponseEntity.ok(supplierService.getSupplierById(id))

    @PostMapping
    fun createSupplier(
        @Valid @RequestBody request: CreateSupplierRequest,
    ): ResponseEntity<SupplierDto> {
        val supplier = supplierService.createSupplier(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(supplier)
    }

    @PutMapping("/{id}")
    fun updateSupplier(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSupplierRequest,
    ): ResponseEntity<SupplierDto> = ResponseEntity.ok(supplierService.updateSupplier(id, request))

    @DeleteMapping("/{id}")
    fun deleteSupplier(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        supplierService.deleteSupplier(id)
        return ResponseEntity.noContent().build()
    }
}
