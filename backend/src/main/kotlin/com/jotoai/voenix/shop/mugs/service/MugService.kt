package com.jotoai.voenix.shop.mugs.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.mugs.dto.CreateMugRequest
import com.jotoai.voenix.shop.mugs.dto.MugDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugRequest
import com.jotoai.voenix.shop.mugs.entity.Mug
import com.jotoai.voenix.shop.mugs.entity.toDto
import com.jotoai.voenix.shop.mugs.repository.MugRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MugService(
    private val mugRepository: MugRepository
) {
    
    fun getAllMugs(): List<MugDto> = mugRepository.findAll().map { it.toDto() }
    
    fun getActiveMugs(): List<MugDto> = mugRepository.findByActiveTrue().map { it.toDto() }
    
    fun getMugById(id: Long): MugDto {
        return mugRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("Mug", "id", id) }
    }
    
    fun searchMugsByName(name: String): List<MugDto> = 
        mugRepository.findByNameContainingIgnoreCaseAndActiveTrue(name).map { it.toDto() }
    
    fun findMugsByPriceRange(minPrice: Int, maxPrice: Int): List<MugDto> =
        mugRepository.findByPriceBetweenAndActiveTrue(minPrice, maxPrice).map { it.toDto() }
    
    @Transactional
    fun createMug(request: CreateMugRequest): MugDto {
        val mug = Mug(
            name = request.name,
            descriptionLong = request.descriptionLong,
            descriptionShort = request.descriptionShort,
            image = request.image,
            price = request.price,
            heightMm = request.heightMm,
            diameterMm = request.diameterMm,
            printTemplateWidthMm = request.printTemplateWidthMm,
            printTemplateHeightMm = request.printTemplateHeightMm,
            fillingQuantity = request.fillingQuantity,
            dishwasherSafe = request.dishwasherSafe,
            active = request.active
        )
        
        val savedMug = mugRepository.save(mug)
        return savedMug.toDto()
    }
    
    @Transactional
    fun updateMug(id: Long, request: UpdateMugRequest): MugDto {
        val mug = mugRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Mug", "id", id) }
        
        request.name?.let { mug.name = it }
        request.descriptionLong?.let { mug.descriptionLong = it }
        request.descriptionShort?.let { mug.descriptionShort = it }
        request.image?.let { mug.image = it }
        request.price?.let { mug.price = it }
        request.heightMm?.let { mug.heightMm = it }
        request.diameterMm?.let { mug.diameterMm = it }
        request.printTemplateWidthMm?.let { mug.printTemplateWidthMm = it }
        request.printTemplateHeightMm?.let { mug.printTemplateHeightMm = it }
        request.fillingQuantity?.let { mug.fillingQuantity = it }
        request.dishwasherSafe?.let { mug.dishwasherSafe = it }
        request.active?.let { mug.active = it }
        
        val updatedMug = mugRepository.save(mug)
        return updatedMug.toDto()
    }
    
    @Transactional
    fun deleteMug(id: Long) {
        if (!mugRepository.existsById(id)) {
            throw ResourceNotFoundException("Mug", "id", id)
        }
        mugRepository.deleteById(id)
    }
}