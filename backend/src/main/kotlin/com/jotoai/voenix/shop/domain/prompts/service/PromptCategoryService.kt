package com.jotoai.voenix.shop.domain.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptCategoryDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.entity.PromptCategory
import com.jotoai.voenix.shop.domain.prompts.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptCategoryService(
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptRepository: PromptRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
) {
    fun getAllPromptCategories(): List<PromptCategoryDto> =
        promptCategoryRepository.findAll().map { category ->
            PromptCategoryDto(
                id = category.id!!,
                name = category.name,
                promptsCount = promptRepository.countByCategoryId(category.id),
                subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
            )
        }

    fun getPromptCategoryById(id: Long): PromptCategoryDto =
        promptCategoryRepository
            .findById(id)
            .map { category ->
                PromptCategoryDto(
                    id = category.id!!,
                    name = category.name,
                    promptsCount = promptRepository.countByCategoryId(category.id),
                    subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                    createdAt = category.createdAt,
                    updatedAt = category.updatedAt,
                )
            }.orElseThrow { ResourceNotFoundException("PromptCategory", "id", id) }

    fun searchPromptCategoriesByName(name: String): List<PromptCategoryDto> =
        promptCategoryRepository.findByNameContainingIgnoreCase(name).map { category ->
            PromptCategoryDto(
                id = category.id!!,
                name = category.name,
                promptsCount = promptRepository.countByCategoryId(category.id),
                subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(category.id).toInt(),
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
            )
        }

    @Transactional
    fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory =
            PromptCategory(
                name = request.name,
            )

        val savedPromptCategory = promptCategoryRepository.save(promptCategory)
        return PromptCategoryDto(
            id = savedPromptCategory.id!!,
            name = savedPromptCategory.name,
            promptsCount = 0, // New category has no prompts
            subcategoriesCount = 0, // New category has no subcategories
            createdAt = savedPromptCategory.createdAt,
            updatedAt = savedPromptCategory.updatedAt,
        )
    }

    @Transactional
    fun updatePromptCategory(
        id: Long,
        request: UpdatePromptCategoryRequest,
    ): PromptCategoryDto {
        val promptCategory =
            promptCategoryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("PromptCategory", "id", id) }

        request.name?.let { promptCategory.name = it }

        val updatedPromptCategory = promptCategoryRepository.save(promptCategory)
        return PromptCategoryDto(
            id = updatedPromptCategory.id!!,
            name = updatedPromptCategory.name,
            promptsCount = promptRepository.countByCategoryId(updatedPromptCategory.id),
            subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(updatedPromptCategory.id).toInt(),
            createdAt = updatedPromptCategory.createdAt,
            updatedAt = updatedPromptCategory.updatedAt,
        )
    }

    @Transactional
    fun deletePromptCategory(id: Long) {
        if (!promptCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("PromptCategory", "id", id)
        }
        promptCategoryRepository.deleteById(id)
    }
}
