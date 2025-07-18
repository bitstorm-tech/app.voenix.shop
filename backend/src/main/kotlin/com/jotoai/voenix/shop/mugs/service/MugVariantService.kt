package com.jotoai.voenix.shop.mugs.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.mugs.dto.CreateMugVariantRequest
import com.jotoai.voenix.shop.mugs.dto.MugVariantDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugVariantRequest
import com.jotoai.voenix.shop.mugs.entity.MugVariant
import com.jotoai.voenix.shop.mugs.repository.MugRepository
import com.jotoai.voenix.shop.mugs.repository.MugVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MugVariantService(
    private val mugVariantRepository: MugVariantRepository,
    private val mugRepository: MugRepository,
) {
    fun getAllMugVariants(): List<MugVariantDto> = mugVariantRepository.findAll().map { it.toDto() }

    fun getMugVariantById(id: Long): MugVariantDto =
        mugVariantRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("MugVariant", "id", id) }

    fun getMugVariantsByMugId(mugId: Long): List<MugVariantDto> {
        if (!mugRepository.existsById(mugId)) {
            throw ResourceNotFoundException("Mug", "id", mugId)
        }
        return mugVariantRepository.findByMugIdOrderByColorCode(mugId).map { it.toDto() }
    }

    @Transactional
    fun createMugVariant(request: CreateMugVariantRequest): MugVariantDto {
        if (!mugRepository.existsById(request.mugId)) {
            throw ResourceNotFoundException("Mug", "id", request.mugId)
        }

        if (mugVariantRepository.existsByMugIdAndColorCode(request.mugId, request.colorCode)) {
            throw ResourceAlreadyExistsException("MugVariant with color code '${request.colorCode}' already exists for this mug")
        }

        val mugVariant =
            MugVariant(
                mugId = request.mugId,
                colorCode = request.colorCode,
                exampleImageFilename = request.exampleImageFilename,
            )

        val savedMugVariant = mugVariantRepository.save(mugVariant)
        return savedMugVariant.toDto()
    }

    @Transactional
    fun updateMugVariant(
        id: Long,
        request: UpdateMugVariantRequest,
    ): MugVariantDto {
        val mugVariant =
            mugVariantRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("MugVariant", "id", id) }

        request.colorCode?.let { newColorCode ->
            if (newColorCode != mugVariant.colorCode &&
                mugVariantRepository.existsByMugIdAndColorCode(mugVariant.mugId, newColorCode)
            ) {
                throw ResourceAlreadyExistsException("MugVariant with color code '$newColorCode' already exists for this mug")
            }
            mugVariant.colorCode = newColorCode
        }
        request.exampleImageFilename?.let { mugVariant.exampleImageFilename = it }

        val updatedMugVariant = mugVariantRepository.save(mugVariant)
        return updatedMugVariant.toDto()
    }

    @Transactional
    fun deleteMugVariant(id: Long) {
        if (!mugVariantRepository.existsById(id)) {
            throw ResourceNotFoundException("MugVariant", "id", id)
        }
        mugVariantRepository.deleteById(id)
    }
}
