package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryFacade
import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryQueryService
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSubCategoryNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSubCategoryServiceImpl(
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptSubCategoryAssembler: PromptSubCategoryAssembler,
) : PromptSubCategoryFacade,
    PromptSubCategoryQueryService {
    override fun getAllPromptSubCategories(): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findAll().map {
            promptSubCategoryAssembler.toDto(it)
        }

    override fun getPromptSubCategoryById(id: Long): PromptSubCategoryDto =
        promptSubCategoryRepository
            .findById(id)
            .map { promptSubCategoryAssembler.toDto(it) }
            .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

    override fun getPromptSubCategoriesByCategory(categoryId: Long): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByPromptCategoryId(categoryId).map { promptSubCategoryAssembler.toDto(it) }

    override fun searchPromptSubCategoriesByName(name: String): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByNameContainingIgnoreCase(name).map { promptSubCategoryAssembler.toDto(it) }

    override fun existsById(id: Long): Boolean = promptSubCategoryRepository.existsById(id)

    @Transactional
    override fun createPromptSubCategory(request: CreatePromptSubCategoryRequest): PromptSubCategoryDto {
        val category =
            promptCategoryRepository
                .findById(request.promptCategoryId)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptCategory", "id", request.promptCategoryId) }

        val promptSubCategory =
            PromptSubCategory(
                name = request.name,
                description = request.description,
                promptCategory = category,
            )

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = promptSubCategoryAssembler.toDto(saved)

        return result
    }

    @Transactional
    override fun updatePromptSubCategory(
        id: Long,
        request: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto {
        val promptSubCategory =
            promptSubCategoryRepository
                .findById(id)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

        val oldDto = promptSubCategoryAssembler.toDto(promptSubCategory)

        request.name?.let { promptSubCategory.name = it }
        request.description?.let { promptSubCategory.description = it }
        request.promptCategoryId?.let { categoryId ->
            val category =
                promptCategoryRepository
                    .findById(categoryId)
                    .orElseThrow { PromptSubCategoryNotFoundException("PromptCategory", "id", categoryId) }
            promptSubCategory.promptCategory = category
        }

        val saved = promptSubCategoryRepository.save(promptSubCategory)
        val result = promptSubCategoryAssembler.toDto(saved)

        return result
    }

    @Transactional
    override fun deletePromptSubCategory(id: Long) {
        val promptSubCategory =
            promptSubCategoryRepository
                .findById(id)
                .orElseThrow { PromptSubCategoryNotFoundException("PromptSubCategory", "id", id) }

        val dto = promptSubCategoryAssembler.toDto(promptSubCategory)
        promptSubCategoryRepository.deleteById(id)
    }
}
